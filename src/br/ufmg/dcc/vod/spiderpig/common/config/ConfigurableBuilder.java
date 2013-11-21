package br.ufmg.dcc.vod.spiderpig.common.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

/**
 * A ConfigurableBuilder object is used to instantiate configurable classes.
 * This is done via reflection, and Configurable classes should have empty 
 * constructors by default.
 * 
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 *
 * @param <T> Return value of the configurable
 */
public class ConfigurableBuilder {

    public <T extends Configurable> T build(Class<T> clazz, 
            Configuration configuration) throws BuildException {
        
        try {
            Constructor<T> constructor =
                    (Constructor<T>) clazz.getConstructor();
            T configurable = (T) constructor.newInstance();
            
            Iterator<String> keys = configuration.getKeys();
            
            Set<String> required = configurable.getRequiredParameters();
            Set<String> toRemove;
            if (required == null) {
                toRemove = new HashSet<>();
            } else {
                toRemove = new HashSet<>(required); //shallow copy
            }
            
            while (keys.hasNext()) {
                String key = keys.next();
                if (toRemove.contains(key))
                    toRemove.remove(key);
            }

            if (!toRemove.isEmpty())
                throw new BuildException("Required keys " + toRemove + 
                        " not found", null);
            
            configurable.configurate(configuration, this);
            return configurable;
        } catch (NoSuchMethodException e) {
            throw new BuildException("Configurable does not have empty"
                    + " constructor. This is necessary!", e);
        } catch (SecurityException e) {
            throw new BuildException("Configurable does not have empty"
                    + " constructor visible. This is necessary!", e);
        } catch (InstantiationException e) {
            throw new BuildException("Unable to instante configurable", e);
        } catch (IllegalAccessException e) {
            throw new BuildException("Unnable to access constructor", e);
        } catch (IllegalArgumentException e) {
            throw new BuildException("Illegal argument to constructor", e);
        } catch (InvocationTargetException e) {
            throw new BuildException("Unable to call constructor", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public final <T extends Configurable> T build(String className,
            Configuration configuration) throws BuildException {
        try {
            return build((Class<T>) Class.forName(className), configuration);
        } catch (ClassNotFoundException e) {
            throw new BuildException("Class not found!", e);
        }
    }
}
