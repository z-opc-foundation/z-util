package com.zifang.util.core.parser.xml;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * 标题请求XML结构。
 * <p>
 * 对应 RequestOrder 根节点，包含区域信息列表。
 *
 * @author zifang
 */
@XmlRootElement(name = "RequestOrder")
/**
 * TitleRequest类。
 */
public class TitleRequest {

    private List<Item> item;

    /**
     * getItem方法。
     *
     * @return List<Item>类型返回值
     */
    public List<Item> getItem() {
        return item;
    }

    /**
     * setItem方法。
     * * @param item ListItem类型参数
     */
    public void setItem(List<Item> item) {
        this.item = item;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "TitleRequest{item=" + item + "}";
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TitleRequest that = (TitleRequest) o;
        return java.util.Objects.equals(item, that.item);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(item);
    }

    @XmlType(propOrder = {"code", "province", "city", "district"})
    public static class Item {

        private String code;
        private String province;
        private String city;
        private String district;

        /**
         * Item方法。
         */
        public Item() {
        }

        /**
         * Item方法。
         * * @param code String类型参数
         *
         * @param province String类型参数
         * @param city     String类型参数
         * @param district String类型参数
         */
        public Item(String code, String province, String city, String district) {
            this.code = code;
            this.province = province;
            this.city = city;
            this.district = district;
        }

        /**
         * getCode方法。
         *
         * @return String类型返回值
         */
        public String getCode() {
            return code;
        }

        /**
         * setCode方法。
         * * @param code String类型参数
         */
        public void setCode(String code) {
            this.code = code;
        }

        /**
         * getProvince方法。
         *
         * @return String类型返回值
         */
        public String getProvince() {
            return province;
        }

        /**
         * setProvince方法。
         * * @param province String类型参数
         */
        public void setProvince(String province) {
            this.province = province;
        }

        /**
         * getCity方法。
         *
         * @return String类型返回值
         */
        public String getCity() {
            return city;
        }

        /**
         * setCity方法。
         * * @param city String类型参数
         */
        public void setCity(String city) {
            this.city = city;
        }

        /**
         * getDistrict方法。
         *
         * @return String类型返回值
         */
        public String getDistrict() {
            return district;
        }

        /**
         * setDistrict方法。
         * * @param district String类型参数
         */
        public void setDistrict(String district) {
            this.district = district;
        }

        @Override
        /**
         * toString方法。
         * @return String类型返回值
         */
        public String toString() {
            return "Item{code=" + code + ", province=" + province + ", city=" + city + ", district=" + district + "}";
        }

        @Override
        /**
         * equals方法。
         *      * @param o Object类型参数
         * @return boolean类型返回值
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Item item = (Item) o;
            return java.util.Objects.equals(code, item.code) &&
                    java.util.Objects.equals(province, item.province) &&
                    java.util.Objects.equals(city, item.city) &&
                    java.util.Objects.equals(district, item.district);
        }

        @Override
        /**
         * hashCode方法。
         * @return int类型返回值
         */
        public int hashCode() {
            return java.util.Objects.hash(code, province, city, district);
        }
    }
}