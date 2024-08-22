package smartin.cubiccart;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;

public class Config {
    public static Config instance = new Config();
    public String drag_calculation = "old_speed - old_speed^2 * 0.004";
    public double copper_speed_per_tick_per_level = 0.1;


    public static double getNewSpeed(double oldSpeed) {
        Expression e = new Expression(instance.drag_calculation.replaceAll("old_speed", "" + oldSpeed));
        try {
            return e.evaluate().getNumberValue().doubleValue();
        } catch (RuntimeException | EvaluationException | ParseException runtimeException) {
            Cubiccarts.LOGGER.error("couldnt evaluate function" + runtimeException);
            return 0.0;
        }
    }
}
