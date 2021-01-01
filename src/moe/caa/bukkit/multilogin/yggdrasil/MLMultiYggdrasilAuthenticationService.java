package moe.caa.bukkit.multilogin.yggdrasil;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserMigratedException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.response.Response;
import moe.caa.bukkit.multilogin.MultiLogin;
import moe.caa.bukkit.multilogin.PluginData;
import moe.caa.bukkit.multilogin.YggdrasilServiceSection;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;

public class MLMultiYggdrasilAuthenticationService extends HttpAuthenticationService {
    private HttpAuthenticationService vanService;
    private final Class YggdrasilAuthenticationServiceClass = YggdrasilAuthenticationService.class;
    private final Field YggdrasilAuthenticationServiceGson = YggdrasilAuthenticationServiceClass.getDeclaredField("gson");
    private Gson gson;

    public MLMultiYggdrasilAuthenticationService() throws NoSuchFieldException {
        super(Proxy.NO_PROXY);
        YggdrasilAuthenticationServiceGson.setAccessible(true);
    }

    @Override
    public UserAuthentication createUserAuthentication(Agent agent) {
        return vanService.createUserAuthentication(agent);
    }

    protected <T extends Response> T makeRequest0(URL url, Object input, Class<T> classOfT) throws AuthenticationException {
        try {
            String jsonResult = input == null ? this.performGetRequest(url) : this.performPostRequest(url, gson.toJson(input), "application/json");
            T result = gson.fromJson(jsonResult, classOfT);
            if (result == null) {
                return null;
            } else if (!PluginData.isBlank(result.getError())) {
                if ("UserMigratedException".equals(result.getCause())) {
                    throw new UserMigratedException(result.getErrorMessage());
                } else if ("ForbiddenOperationException".equals(result.getError())) {
                    throw new InvalidCredentialsException(result.getErrorMessage());
                } else {
                    throw new AuthenticationException(result.getErrorMessage());
                }
            } else {
                return result;
            }
        } catch (IllegalStateException | JsonParseException | IOException var6) {
            throw new AuthenticationUnavailableException("Cannot contact authentication server", var6);
        }
    }

    public <T extends Response> T makeRequest(URL url, Object input, Class<T> classOfT) throws AuthenticationException {
        if(classOfT != MLHasJoinedMinecraftServerResponse.class) return makeRequest0(url, input, classOfT);
        T ret = null;
        boolean down = false;
        Map<Future<T>, YggdrasilServiceSection> tasks = new HashMap<>();
        String arg = null;
        for(String s : url.toString().split("/")){
            if(s.startsWith("hasJoined?")){
                arg = s;
            }
        }
        if(PluginData.isOfficialYgg()){
            Future<T> task = Bukkit.getScheduler().callSyncMethod(MultiLogin.INSTANCE, () -> this.makeRequest0(url, input, classOfT));
            tasks.put(task, YggdrasilServiceSection.OFFICIAL);
        }
        String finalArg = arg;
        for(YggdrasilServiceSection section : PluginData.getServiceSet()){
            Future<T> task = Bukkit.getScheduler().callSyncMethod(MultiLogin.INSTANCE, () -> this.makeRequest0(section.buildUrl(finalArg), input, classOfT));
            tasks.put(task, section);
        }

        Future<T> taskDown = null;
        long time = System.currentTimeMillis() + PluginData.getTimeOut();
        dos:while(time > System.currentTimeMillis() && tasks.size() != 0){
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
        cancelAll(tasks);

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

    private <T>void cancelAll(Map<Future<T>, YggdrasilServiceSection> tasks){
        for (Future<T> future : tasks.keySet()){
            future.cancel(true);
        }
    }

    @Override
    public MinecraftSessionService createMinecraftSessionService() {
        try {
            return new MLMultiYggdrasilMinecraftSessionService(this);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public GameProfileRepository createProfileRepository() {
        return vanService.createProfileRepository();
    }

    public void setVanService(HttpAuthenticationService vanService) throws IllegalAccessException {
        this.vanService = vanService;

        gson = (Gson) YggdrasilAuthenticationServiceGson.get(vanService);
    }
}
