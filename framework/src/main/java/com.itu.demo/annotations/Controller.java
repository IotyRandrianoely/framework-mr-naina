package mg.framework.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

}
/*
sprint 2 bis
on sait si la classe et annoter controller !!

sprint 3
si on tape une url on doit savoir cettte url vient de cette class (controller) et de cette methode annote
*/
// init de frontservlet ou context initializer
//init() on va mapperd - dans FrontServlet

//sprint 4 , invoquena 