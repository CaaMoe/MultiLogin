package fun.ksnb.multilogin.velocity.pccsh;

public interface IPccsh {
    static String getPccshName() {
        try {
            Class.forName("com.velocitypowered.proxy.connection.client.LoginSessionHandler");
            return "v3_1_1";
        } catch (ClassNotFoundException e) {
            return "v3_1_2";
        }
    }
}
