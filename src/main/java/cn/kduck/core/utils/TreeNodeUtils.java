package cn.kduck.core.utils;

import cn.kduck.core.service.ValueMap;
import com.fasterxml.jackson.annotation.JsonBackReference;
import cn.kduck.core.service.ValueMapList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * LiuHG
 */
public final class TreeNodeUtils {

    private TreeNodeUtils() {}

    public static List<Node<ValueMap>> formatTreeNode(ValueMapList dataList, String id, String name, String pid){
        LinkedHashMap<String,Node<ValueMap>> nodeMap = new LinkedHashMap<>();
        for (ValueMap data : dataList) {
            Node<ValueMap> node = new Node<>(data);
            String idValue = data.getValueAsString(id);
            node.setId(idValue);
            node.setTitle(data.getValueAsString(name));
            nodeMap.put(idValue,node);
        }
        List<String> hasParentList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            ValueMap data = dataList.get(i);

            String orgId = data.getValueAsString(id);
            String parentId = data.getValueAsString(pid);

            if(orgId.equals(parentId)) {
                continue;
            }

            Node<ValueMap> parentNode = nodeMap.get(parentId);
            if (parentNode != null) {
                Node<ValueMap> node = nodeMap.get(orgId);
                parentNode.getChildren().add(node);
                hasParentList.add(orgId);
            }
        }
        for (String nodeID : hasParentList) {
            nodeMap.remove(nodeID);
        }
        return new ArrayList<>(nodeMap.values());
    }

    public static <T> List<Node<T>> formatTreeNode(List<T> dataList, Function<T, Serializable> id, Function<T,String> name, Function<T,Serializable> pid){
        LinkedHashMap<String,Node<T>> nodeMap = new LinkedHashMap<>();
        for (T data : dataList) {
            Node<T> node = new Node<>(data);
            Serializable idValue = id.apply(data);
            node.setId(idValue.toString());
            node.setTitle(name.apply(data));
            nodeMap.put(idValue.toString(),node);
        }
        List<String> hasParentList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);

            Serializable orgId = id.apply(data);
            Serializable parentId = pid.apply(data);

            if(orgId.equals(parentId)) {
                continue;
            }

            Node<T> parentNode = nodeMap.get(parentId);
            if (parentNode != null) {
                Node<T> node = nodeMap.get(orgId);
                parentNode.getChildren().add(node);
                hasParentList.add(orgId.toString());
            }
        }
        for (String nodeID : hasParentList) {
            nodeMap.remove(nodeID);
        }
        return new ArrayList<>(nodeMap.values());
    }


    public static class Node<T>{

        private String id;
//        @JsonManagedReference
        private List<Node<T>> children = new ArrayList<>();

        @JsonBackReference
        private Node<T> parent;
        private String title;

        private final T data;

        public Node() {
            this.data = null;
        }

        public Node(T data) {
            this.data = data;
        }

        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public List<Node<T>> getChildren() {
            return children;
        }
        public void setChildren(List<Node<T>> children) {
            this.children = children;
        }
        public Node<T> getParent() {
            return parent;
        }
        public void setParent(Node<T> parent) {
            this.parent = parent;
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public boolean hasChildren() {
            return !children.isEmpty();
        }

        public T getData() {
            return data;
        }
    }
}
