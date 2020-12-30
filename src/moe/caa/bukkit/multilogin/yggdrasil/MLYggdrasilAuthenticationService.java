package moe.caa.bukkit.multilogin.yggdrasil;

import com.mojang.authlib.Environment;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.response.Response;
import moe.caa.bukkit.multilogin.PluginData;
import moe.caa.bukkit.multilogin.MultiLogin;
import moe.caa.bukkit.multilogin.YggdrasilServiceSection;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.net.Proxy;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Future;

public class MLYggdrasilAuthenticationService extends YggdrasilAuthenticationService {
    private final MultiLogin ML;
    private static Field environment;

    static {
        try {
            environment = YggdrasilAuthenticationService.class.getDeclaredField("environment");
            environment.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public MLYggdrasilAuthenticationService(MultiLogin ml) {
        super(Proxy.NO_PROXY, null);
        this.ML = ml;
    }

    private  <T extends Response> T makeRequest0(URL url, Object input, Class<T> classOfT) throws AuthenticationException {
        return super.makeRequest(url, input, classOfT);
    }


    @Override
    public <T extends Response> T makeRequest(URL url, Object input, Class<T> classOfT) throws AuthenticationException {
        T ret = null;
        boolean down = false;
        Map<Future<T>, YggdrasilServiceSection> tasks = new HashMap();
        String arg = null;
        for(String s : url.toString().split("/")){
            if(s.startsWith("hasJoined?")){
                arg = s;
            }
        }
        if(PluginData.isOfficialYgg()){
            Future<T> task = Bukkit.getScheduler().callSyncMethod(ML, () -> this.makeRequest0(url, input, classOfT));
            tasks.put(task, YggdrasilServiceSection.OFFICIAL);
        }
        String finalArg = arg;
        for(YggdrasilServiceSection section : PluginData.Services.values()){
            Future<T> task = Bukkit.getScheduler().callSyncMethod(ML, () -> this.makeRequest0(section.buildUrl(finalArg), input, classOfT));
            tasks.put(task, section);
        }

        Future<T> taskDown = null;
        long time = System.currentTimeMillis() + PluginData.getTimeOut();
        dos:while(time > System.currentTimeMillis()){
            Iterator<Future<T>> itr = tasks.keySet().iterator();
            while (itr.hasNext()){
                Future<T> task = itr.next();
                if(task.isDone()){
                    try {
                        ret = task.get();
                    } catch (Exception ignored) {
                        down = true;
                    }
                    if(ret != null){
                        taskDown = task;
                        break dos;
                    }
                    itr.remove();
                }
            }
        }

        if(ret == null){
            if(down)
                throw new AuthenticationUnavailableException();
            return null;
        }
        if(ret instanceof MLHasJoinedMinecraftServerResponse){
            ((MLHasJoinedMinecraftServerResponse) ret).setYggService(tasks.get(taskDown));
        }
        return ret;
    }

    @Override
    public MinecraftSessionService createMinecraftSessionService() {
        try {
            return new MLYggdrasilMinecraftSessionService(this, (Environment) environment.get(this));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
