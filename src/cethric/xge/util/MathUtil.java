package cethric.xge.util;

import javax.vecmath.Quat4d;
import javax.vecmath.Quat4f;

/**
 * Created by blakerogan on 27/01/15.
 */
public class MathUtil {
    public static double[] QuatToEuler(Quat4d q1) {
        double sqw = q1.w*q1.w;
        double sqx = q1.x*q1.x;
        double sqy = q1.y*q1.y;
        double sqz = q1.z*q1.z;
        double unit = sqx + sqy + sqz + sqw; // if normalised is one, otherwise is correction factor
        double test = q1.x*q1.y + q1.z*q1.w;
        if (test > 0.499*unit) { // singularity at north pole
            double heading = 2 * Math.atan2(q1.x, q1.w);
            double attitude = Math.PI/2;
            double bank = 0;
            return new double[] {heading, attitude, bank};
        }
        if (test < -0.499*unit) { // singularity at south pole
            double heading = -2 * Math.atan2(q1.x, q1.w);
            double attitude = -Math.PI/2;
            double bank = 0;
            return new double[] {heading, attitude, bank};
        }
        double heading = Math.atan2(2 * q1.y * q1.w - 2 * q1.x * q1.z, sqx - sqy - sqz + sqw);
        double attitude = Math.asin(2 * test / unit);
        double bank = Math.atan2(2 * q1.x * q1.w - 2 * q1.y * q1.z, -sqx + sqy - sqz + sqw);
        return new double[] {heading, attitude, bank};
    }

    public static double[] QuatToEuler(Quat4f quat4f) {
        return QuatToEuler(new Quat4d(quat4f));
    }
}
