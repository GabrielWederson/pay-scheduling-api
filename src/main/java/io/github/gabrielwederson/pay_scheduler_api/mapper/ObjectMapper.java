package io.github.gabrielwederson.pay_scheduler_api.mapper;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;

import java.util.ArrayList;
import java.util.List;

public class ObjectMapper {

    private static final Mapper mapper = DozerBeanMapperBuilder.buildDefault();

    public static <O, D> D parseObjectMapper(O origin, Class<D> destination){
        return mapper.map(origin, destination);
    }

    public static <O, D> List<D> parseListObjectMapper(List<O> origin, Class<D> destination){
       List<D> destinationObjects = new ArrayList<>();
        for(Object o: origin){
            destinationObjects.add(mapper.map(o, destination));
        }
        return destinationObjects;
    }
}
