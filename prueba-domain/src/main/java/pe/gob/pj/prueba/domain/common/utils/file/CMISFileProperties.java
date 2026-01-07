package pe.gob.pj.prueba.domain.common.utils.file;


import java.util.HashMap;
import java.util.Map;

public class CMISFileProperties {


    Map<String, Object> properties = new HashMap<String, Object>();

    public void addProp(String name, Object value){
        properties.put(name, value);
    }

    public Map<String, Object> getProperties(){
        return properties;
    }

}
