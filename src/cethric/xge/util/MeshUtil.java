package cethric.xge.util;

import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by blakerogan on 5/04/15.
 */
public class MeshUtil {
    public MeshUtil() {
        // Nothing to do here
    }

    public static Object[] indexVertexArray(FloatBuffer vertexArray) {
        System.out.printf("Initial Size: %d%n", vertexArray.limit()/3);
        List<Vector3f> indexedValues = new ArrayList<Vector3f>();
        List<Integer> indexOrder = new ArrayList<Integer>();
        int size = vertexArray.limit()/3;
        for (int i = 0; i < size; i++) {
            float x,y,z;
            x = vertexArray.get(i*3);
            y = vertexArray.get(i*3+1);
            z = vertexArray.get(i*3+2);
            Vector3f vector3f = new Vector3f(x, y, z);
            if (indexedValues.contains(vector3f)) {
//                System.out.println("Already Indexed");
                int index = indexedValues.indexOf(vector3f);
                indexOrder.add(index);
            } else {
//                System.out.println("Indexing");
                indexedValues.add(vector3f);
                int index = indexedValues.indexOf(vector3f);
                indexOrder.add(index);
            }
        }

        System.out.printf("Final Size: %d%n", indexedValues.size());

        System.out.println(indexedValues);
        System.out.println(indexOrder);

        return new Object[] {indexedValues, indexOrder};
    }
}
