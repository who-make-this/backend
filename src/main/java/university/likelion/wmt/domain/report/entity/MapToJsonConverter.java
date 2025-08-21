package university.likelion.wmt.domain.report.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Map;

@Converter
public class MapToJsonConverter implements AttributeConverter<Map<String, Integer>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(Map<String, Integer> attribute) {
        if(attribute == null){
            return null;
        }
        try{
            return objectMapper.writeValueAsString(attribute);
        }catch(JsonProcessingException e){
            throw new IllegalArgumentException("Map을 JSON 문자열로 변환하는 중 오류가 발생했습니다.", e);

        }
    }
    @Override
    public Map<String, Integer> convertToEntityAttribute(String dbData) {
        if(dbData == null || dbData.isEmpty()){
            return null;
        }
        try{
            return objectMapper.readValue(dbData, new TypeReference<>() {});
            }catch(IOException e){
            throw new IllegalArgumentException("JSON 문자열을 Map으로 변환하는 중 오류가 발생했습니다.", e);
        }
    }
}
