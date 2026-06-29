package io.github.defective4.audioanalyzer.config.yaml;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YamlMapper {
    public Map<String, Object> dump(Record record)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        if (record == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        for (RecordComponent cpt : record.getClass().getRecordComponents()) {
            Field field = record.getClass().getDeclaredField(cpt.getName());
            field.setAccessible(true);
            Object value = field.get(record);
            map.put(field.getName(), transformValue(value));
        }
        return Collections.unmodifiableMap(map);
    }

    public <T> T load(Map<String, Object> map, Class<T> rClass)
            throws NoSuchFieldException, SecurityException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (rClass == null || !rClass.isRecord()) return null;
        List<Object> params = new ArrayList<>();

        Constructor<T> constructor = (Constructor<T>) rClass.getConstructors()[0];
        for (Parameter param : constructor.getParameters()) {
            Class<?> type = param.getType();
            Object value = map.get(param.getName());
            if (value == null) {
                params.add(null);
            } else if (Record.class.isAssignableFrom(type) && Map.class.isAssignableFrom(value.getClass())) {
                Map<String, Object> submap = (Map<String, Object>) value;
                params.add(load(submap, type));
            } else if (type.isAssignableFrom(value.getClass())) {
                params.add(type.cast(value));
            } else {
                params.add(value);
            }
        }

        return (T) constructor.newInstance(params.toArray(Object[]::new));
    }

    private Object transformValue(Object value) throws NoSuchFieldException, IllegalAccessException {
        if (value == null) return null;
        Class<?> class1 = value.getClass();
        if (List.class.isAssignableFrom(class1)) {
            List<Object> transformed = new ArrayList<>();
            for (Object obj : (List<?>) value) transformed.add(transformValue(obj));
            return Collections.unmodifiableList(transformed);
        } else if (class1.isRecord()) {
            return dump((Record) value);
        }
        return value;
    }
}
