/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.auth.AuthErrorEnum
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.auth;

/**
 * Yggdrasil验证失败类型枚举
 */
public enum AuthErrorEnum {

    /**
     * 由于宕机或无法连接
     */
    SERVER_DOWN,

    /**
     * 身份验证失败
     */
    VALIDATION_FAILED,

    /**
     * 没有服务器
     */
    NO_SERVER
}
