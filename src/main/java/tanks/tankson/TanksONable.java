package tanks.tankson;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Objects implementing this may be TanksON-ed */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TanksONable
{
    String value();
}
