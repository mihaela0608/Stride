package org.example.stride.config;

import org.example.stride.model.dto.RunCreationDto;
import org.example.stride.model.entity.Run;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {
    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(RunCreationDto.class, Run.class)
                .addMappings(mapper -> mapper.skip(Run::setId));

        return modelMapper;
    }
}
