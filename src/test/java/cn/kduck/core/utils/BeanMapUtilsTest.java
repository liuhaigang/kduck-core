package cn.kduck.core.utils;

import cn.kduck.core.service.ParamMap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class BeanMapUtilsTest {

    @Test
    public void toMap() {
        ParentObject parentObject = new ParentObject();
        parentObject.setText("i'm parent");
        parentObject.setDate(new Date());
        parentObject.setBool(true);
        parentObject.setDecimalDouble(3.14);
        parentObject.setDecimalFloat(1.414F);
        parentObject.setNum(317);
        parentObject.setTextArray(new String[]{"a","b","c"});
        parentObject.setValueMap(ParamMap.create().set("name","张三").set("age",18).toMap());

        SubObject subObject1 = new SubObject();
        subObject1.setSonText("i'm big son");
        subObject1.setSonBool(false);
        subObject1.setSonDecimalFloat(19.82F);
        subObject1.setSonDecimalDouble(41.3);
        subObject1.setSonNum(1982);
        parentObject.setSubObject(subObject1);

        SubObject subObject2 = new SubObject();
        subObject2.setSonText("i'm small son");
        subObject2.setSonBool(false);
        subObject2.setSonDecimalFloat(19.82F);
        subObject2.setSonDecimalDouble(41.3);
        subObject2.setSonNum(1982);

        parentObject.setSubList(Arrays.asList(subObject1,subObject2));

        long begin = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
        Map<String, Object> valueMap = BeanMapUtils.toMap(parentObject);
        }
        System.out.println("耗费：" + (System.currentTimeMillis() - begin));

//        Assert.assertEquals(valueMap.get("text"),"i'm parent");
//        Assert.assertEquals(valueMap.get("text").getClass(),String.class);
//
//        Assert.assertNotNull(valueMap.get("date"));
//        Assert.assertEquals(valueMap.get("date").getClass(),Date.class);
//
//        Assert.assertEquals(317,valueMap.get("num"));
//        Assert.assertEquals(Integer.class,valueMap.get("num").getClass());
//
//        Object son = valueMap.get("subObject");
//        Assert.assertNotNull(son);
//        Assert.assertTrue(Map.class.isAssignableFrom(son.getClass()));
//        Assert.assertEquals(41.3,((Map)son).get("sonDecimalDouble"));
//        Assert.assertEquals(false,((Map)son).get("sonBool"));
//
//        ParentObject bean = BeanMapUtils.toBean(valueMap, ParentObject.class);
//
//        ObjectMapper om = new ObjectMapper();
//        try {
//            String s = om.writeValueAsString(parentObject);
//            om.readValue(s,Map.class);
//            om.readValue(s,ParentObject.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        Assert.assertNotNull(bean.getSubObject());
//        Assert.assertEquals(true,bean.getBool());
//        Assert.assertEquals("i'm parent",bean.getText());
//        Assert.assertEquals("i'm big son",bean.getSubObject().getSonText());
    }

//    @Test
//    public void toBean() {
//    }
}