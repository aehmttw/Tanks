package tanks.tankson;

import tanks.*;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * A class which allows users to define custom functions based on field type
 * to deserialize and serialize objects along a stream.
 * */
public class ReflectionHandle<S>
{
    protected static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    public Map<Class<?>, List<FieldHandle>> fieldsInClass = new HashMap<>();
    public Map<Class<?>, TypeHandle<S, ?>> typeHandles = new HashMap<>();

    public Function<Field, Boolean> fieldFilter = f -> true;

    public boolean superclassFields = false;

    public void readObject(S stream, Object object)
    {
        for (FieldHandle f : getFieldsInClass(object.getClass()))
            f.write(object, getTypeHandle(f.field.getType()).read(stream));
    }

    public void writeObject(S stream, Object object)
    {
        for (FieldHandle f : getFieldsInClass(object.getClass()))
            getTypeHandle(f.field.getType()).write(stream, f.read(object));
    }

    @SuppressWarnings("unchecked")
    public <T> TypeHandle<S, T> getTypeHandle(Class<T> type)
    {
        TypeHandle<S, T> h = (TypeHandle<S, T>) typeHandles.get(type);
        if (h != null)
            return h;

        throw new MissingHandleException("Failed to find type handle" +
            " for " + type + ". Either register a type handle for it, or modify the fieldFilter to skip" +
            " registering the field.");
    }

    public <T> ReflectionHandle<S> registerTypeHandle(Class<T> castType, Function<S, ?> read, BiConsumer<S, T> write)
    {
        return registerTypeHandle(castType, castType, read, write);
    }

    public <O, T> ReflectionHandle<S> registerTypeHandle(Class<O> originalType, Class<T> castType, Function<S, ?> read, BiConsumer<S, T> write)
    {
        typeHandles.put(originalType, new TypeHandle<>(castType, read, write));
        return this;
    }

    public List<FieldHandle> getFieldsInClass(Class<?> c)
    {
        return fieldsInClass.computeIfAbsent(c, k ->
        {
            List<FieldHandle> fields = new ArrayList<>();
            Class<?> clazz = c;

            do
            {
                for (Field f : clazz.getDeclaredFields())
                {
                    if (shouldCheckField(f))
                        fields.add(new FieldHandle(f));
                }
                clazz = clazz.getSuperclass();
            } while (superclassFields && clazz != null && clazz != Object.class);
            return fields;
        });
    }

    public ReflectionHandle<S> setFieldFilter(Function<Field, Boolean> fieldFilter)
    {
        this.fieldFilter = fieldFilter;
        return this;
    }

    public boolean shouldCheckField(Field f)
    {
        return !Modifier.isStatic(f.getModifiers()) && fieldFilter.apply(f);
    }

    public static class FieldHandle
    {
        protected final MethodHandle read, write;
        protected final Field field;

        public FieldHandle(Field field)
        {
            try
            {
                field.setAccessible(true);
                this.read = lookup.unreflectGetter(field);
                this.write = lookup.unreflectSetter(field);

                this.field = field;
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(this + ": " + e);
            }
        }

        public Object read(Object obj)
        {
            try
            {
                return this.read.invoke(obj);
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e);
            }
        }

        public void write(Object obj, Object value)
        {
            try
            {
                this.write.invoke(obj, value);
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public static class TypeHandle<I, T>
    {
        protected final Class<T> castType;
        protected Function<I, ?> read;
        protected BiConsumer<I, T> write;

        protected TypeHandle(Class<T> castType, Function<I, ?> readFunc, BiConsumer<I, T> writeFunc)
        {
            this.castType = castType;
            this.read = readFunc;
            this.write = writeFunc;
        }

        public T read(I object)
        {
            return castType.cast(read.apply(object));
        }

        @SuppressWarnings("unchecked")
        public void write(I stream, Object object)
        {
            write.accept(stream, (T) object);
        }
    }

    public static class MissingHandleException extends RuntimeException
    {
        public MissingHandleException(String message)
        {
            super(message);
        }
    }
}
