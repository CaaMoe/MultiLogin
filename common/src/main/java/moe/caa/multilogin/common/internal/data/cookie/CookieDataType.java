package moe.caa.multilogin.common.internal.data.cookie;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CookieDataType {
    String value();
}
