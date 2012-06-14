package org.jboss.arquillian.extension.jrebel.shrinkwrap;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;

import java.io.File;
import java.lang.reflect.Field;

public final class AssetHelper {
// -------------------------- STATIC METHODS --------------------------

    public static Class<?> getClass(ClassAsset asset)
    {
        return (Class<?>) getFieldValueByReflection(asset, "clazz");
    }

    private static Object getFieldValueByReflection(Asset asset, String fieldName)
    {
        try {
            Field privateField = asset.getClass().getDeclaredField(fieldName);
            privateField.setAccessible(true);
            return privateField.get(asset);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Cannot read file field from asset", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot read file field from asset", e);
        }
    }

    public static File getFile(FileAsset asset)
    {
        return (File) getFieldValueByReflection(asset, "file");
    }

// --------------------------- CONSTRUCTORS ---------------------------

    private AssetHelper()
    {
    }
}
