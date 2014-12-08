package com.hitchh1k3rsguide.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.lwjgl.system.glfw.GLFW;

import com.hitchh1k3rsguide.ld31.Main;

public class GenericUtils
{

    public static float lerp(float a, float b, double interpolation)
    {
        return (float) ((b - a) * interpolation + a);
    }

    public static void crash(String message)
    {
        GLFW.glfwHideWindow(Main.instance.windowID);
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(-1);
    }

    public static UUID getUUID(String path)
    {
        try
        {
            byte[] bytes = new byte[Long.SIZE / 8 * 2];

            FileInputStream fis = new FileInputStream(path);
            fis.read(bytes);
            fis.close();

            ByteBuffer longBuffer = ByteBuffer.allocate(Long.SIZE / 8 * 2);
            longBuffer.put(bytes, 0, Long.SIZE / 8 * 2);
            longBuffer.flip();
            long least = longBuffer.getLong();
            long most = longBuffer.getLong();

            // return new UUID(most, least);
        }
        catch (Exception e)
        {
        }
        return UUID.randomUUID();
    }

    public static void stashUUID(String path, UUID uuid)
    {
        try
        {
            ByteBuffer longBuffer = ByteBuffer.allocate(Long.SIZE / 8 * 2);
            longBuffer.putLong(uuid.getLeastSignificantBits());
            longBuffer.putLong(uuid.getMostSignificantBits());
            longBuffer.flip();

            byte[] bytes = longBuffer.array();

            FileOutputStream fos = new FileOutputStream(path);
            fos.write(bytes);
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String loadTextFile(String internalPath)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                GenericUtils.class.getResourceAsStream(internalPath)));
        StringBuilder out = new StringBuilder();
        String line;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                out.append(line);
                out.append('\n');
            }
        }
        catch (Exception e)
        {
        }
        return out.toString();
    }

    /**
     * Compute the absolute file path to the jar file. The framework is based on
     * http://stackoverflow.com/a/12733172/1614775 But that gets it right for only one of the four
     * cases.
     * 
     * @param aclass
     *            A class residing in the required jar.
     * 
     * @return A File object for the directory in which the jar file resides. During testing with
     *         NetBeans, the result is ./build/classes/, which is the directory containing what will
     *         be in the jar.
     */
    public static File getJarDir()
    {
        URL url;
        String extURL; //  url.toExternalForm();

        // get an url
        try
        {
            url = GenericUtils.class.getProtectionDomain().getCodeSource().getLocation();
            // url is in one of two forms
            //        ./build/classes/   NetBeans test
            //        jardir/JarName.jar  froma jar
        }
        catch (SecurityException ex)
        {
            url = GenericUtils.class.getResource(GenericUtils.class.getSimpleName() + ".class");
            // url is in one of two forms, both ending "/com/physpics/tools/ui/PropNode.class"
            //          file:/U:/Fred/java/Tools/UI/build/classes
            //          jar:file:/U:/Fred/java/Tools/UI/dist/UI.jar!
        }

        // convert to external form
        extURL = url.toExternalForm();

        // prune for various cases
        if (extURL.endsWith(".jar")) // from getCodeSource
            extURL = extURL.substring(0, extURL.lastIndexOf("/"));
        else
        { // from getResource
            String suffix = "/" + (GenericUtils.class.getName()).replace(".", "/") + ".class";
            extURL = extURL.replace(suffix, "");
            if (extURL.startsWith("jar:") && extURL.endsWith(".jar!"))
                extURL = extURL.substring(4, extURL.lastIndexOf("/"));
        }

        // convert back to url
        try
        {
            url = new URL(extURL);
        }
        catch (MalformedURLException mux)
        {
            // leave url unchanged; probably does not happen
        }

        // convert url to File
        try
        {
            return new File(url.toURI());
        }
        catch (URISyntaxException ex)
        {
            return new File(url.getPath());
        }
    }

}
