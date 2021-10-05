package moe.caa.multilogin.core.command;

/**
 * 代表一个命令
 */
public class CommandArguments {
    private final String[] args;
    private int offset = 0;

    /**
     * 构建这个命令
     *
     * @param args 命令参数
     */
    public CommandArguments(String[] args) {
        this.args = args;
    }

    /**
     * 构建这个命令
     *
     * @param command 根命令名称
     * @param args    命令参数
     */
    public CommandArguments(String command, String[] args) {
        String[] ns = new String[args.length + 1];
        System.arraycopy(args, 0, ns, 1, args.length);
        ns[0] = command;
        this.args = ns;
    }

    /**
     * 偏移
     *
     * @param offset 偏移量
     * @return 偏移后偏移量
     */
    public int offset(int offset) {
        return this.offset += offset;
    }

    /**
     * 得到指定位置的命令参数
     *
     * @param index 相对位置
     * @return 命令参数
     */
    public String getIndex(int index) {
        return args[index + offset];
    }

    /**
     * 获得参数长度
     *
     * @return 参数长度
     */
    public int getLength() {
        return args.length - offset;
    }

    /**
     * 得到指定位置的命令参数，不经过偏移
     *
     * @param index 指定位置
     * @return 命令参数
     */
    public String getRawIndex(int index) {
        return args[index];
    }

    /**
     * 获得原始参数长度
     *
     * @return 参数长度
     */
    public int getRawLength() {
        return args.length - offset;
    }

    /**
     * 返回完整命令和其参数
     *
     * @return 完整命令和其参数
     */
    @Override
    public String toString() {
        return "/" + String.join(" ", args);
    }
}
