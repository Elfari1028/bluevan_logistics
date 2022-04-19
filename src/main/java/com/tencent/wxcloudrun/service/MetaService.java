package com.tencent.wxcloudrun.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.tencent.wxcloudrun.config.L;
import com.tencent.wxcloudrun.model.MetaConfig;
import com.tencent.wxcloudrun.model.util.GlobalMetaFields;
import com.tencent.wxcloudrun.repo.MetaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MetaService {
    private MetaRepo metaRepo;

    public MetaService(@Autowired MetaRepo metaRepo){
        this.metaRepo = metaRepo;
        initFields();
    }

    public void initFields(){
        for (GlobalMetaFields field:
        GlobalMetaFields.values()) {
            if(!metaRepo.findByFieldName(field.value).isPresent()){
                MetaConfig config = new MetaConfig();
                config.setFieldName(field.value);
                switch (field){
                    case packageType:
                    case cargoType:
                        config.setValue( (new JSONArray()).toString());break;
                    case banlist:
                        config.setValue("");
                }
                metaRepo.save(config);
            }
        }
    }

    public String get(GlobalMetaFields field){
        Optional<MetaConfig> config =  metaRepo.findByFieldName(field.value);
        if(!config.isPresent()){
            initFields();
            config =  metaRepo.findByFieldName(field.value);
        }
        return config.get().getValue();
    }
    public void set(GlobalMetaFields field, Object str){
        Optional<MetaConfig> configOp =  metaRepo.findByFieldName(field.value);
        MetaConfig config = configOp.get();
        config.setValue(str.toString());
        metaRepo.save(config);
    }


    public List<String> getPackageTypes(){
      String raw = get(GlobalMetaFields.packageType);
        return JSON.parseArray(raw).toJavaList(String.class);
    }

    public List<String> getCargoTypes(){
        String raw = get(GlobalMetaFields.cargoType);
        return JSON.parseArray(raw).toJavaList(String.class);
    }

    public String getBanlist(){
        return get(GlobalMetaFields.banlist);
    }
    public void setPackageTypes(List<String> stringList){
        set(GlobalMetaFields.packageType,stringList);
    }
    public void setCargoTypes(List<String> stringList){
        set(GlobalMetaFields.cargoType,stringList);
    }
    public void setBanlist(String banlist){
        set(GlobalMetaFields.banlist,banlist);
    }


}
