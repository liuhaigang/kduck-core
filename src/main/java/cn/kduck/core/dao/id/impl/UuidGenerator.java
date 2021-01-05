package cn.kduck.core.dao.id.impl;

import cn.kduck.core.dao.id.IdGenerator;

import java.io.Serializable;
import java.util.UUID;

/**
 * LiuHG
 */
public class UuidGenerator implements IdGenerator {
    @Override
    public Serializable nextId() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }
}
