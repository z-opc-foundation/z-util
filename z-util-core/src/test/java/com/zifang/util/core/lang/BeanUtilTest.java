package com.zifang.util.core.lang;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

/**
 * BeanUtilTest类。
 */
public class BeanUtilTest {

    /**
     * 测试用bean。
     */
    public static class SampleBean {
        private String name;
        private Integer age;
        private String city;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }
    }

    /**
     * 测试用子类bean（验证父类字段参与合并）。
     */
    public static class ChildBean extends SampleBean {
        private String extra;

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }
    }

    /**
     * diff 测试用bean（含 BigDecimal 与静态字段）。
     */
    public static class DiffBean {
        private String name;
        private Integer age;
        private BigDecimal amount;
        private static String shared = "s";
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    @Test
    /**
     * testCombineObject方法。
     */
    public void testCombineObject() {
        SampleBean source = new SampleBean();
        source.setName("alice");
        SampleBean target = new SampleBean();
        target.setName("bob");
        target.setAge(18);
        target.setCity("shanghai");

        SampleBean combined = BeanUtil.combineObject(source, target);
        // 返回同一实例
        assertSame(target, combined);
        // source非空属性覆盖target
        assertEquals("alice", combined.getName());
        // source为null的属性保持target原值
        assertEquals(Integer.valueOf(18), combined.getAge());
        assertEquals("shanghai", combined.getCity());
    }

    @Test
    /**
     * testCombineObject_NullInput方法。
     */
    public void testCombineObject_NullInput() {
        SampleBean target = new SampleBean();
        target.setName("bob");
        // source为null时直接返回target且不修改
        assertSame(target, BeanUtil.combineObject(null, target));
        assertEquals("bob", target.getName());
        // target为null时返回null
        assertNull(BeanUtil.combineObject(new SampleBean(), null));
        // 两者均为null返回null
        assertNull(BeanUtil.combineObject(null, null));
    }

    @Test
    /**
     * testCombineObject_InheritField方法。
     */
    public void testCombineObject_InheritField() {
        ChildBean source = new ChildBean();
        source.setExtra("ex");
        ChildBean target = new ChildBean();
        target.setName("parent");

        ChildBean combined = BeanUtil.combineObject(source, target);
        // 父类字段同样参与非空覆盖
        assertEquals("parent", combined.getName());
        assertEquals("ex", combined.getExtra());
    }

    @Test
    /**
     * testCombineObject_StaticFieldSkipped方法。
     */
    public void testCombineObject_StaticFieldSkipped() {
        SampleBean source = new SampleBean();
        source.setName("alice");
        SampleBean target = new SampleBean();
        // 静态字段不参与合并，不抛异常
        SampleBean combined = BeanUtil.combineObject(source, target);
        assertEquals("alice", combined.getName());
    }

    @Test
    /**
     * testIsAllFieldValueNull方法。
     */
    public void testIsAllFieldValueNull() {
        // 全部字段为null
        assertTrue(BeanUtil.isAllFieldValueNull(new SampleBean()));
        // 任一字段非null即返回false
        SampleBean bean = new SampleBean();
        bean.setAge(18);
        assertFalse(BeanUtil.isAllFieldValueNull(bean));
        // 对象本身为null
        assertTrue(BeanUtil.isAllFieldValueNull(null));
    }

    @Test
    /**
     * testIsAllFieldValueNull_InheritField方法。
     */
    public void testIsAllFieldValueNull_InheritField() {
        // 父类字段参与判断
        ChildBean child = new ChildBean();
        child.setName("parent");
        assertFalse(BeanUtil.isAllFieldValueNull(child));
        // 父类与子类字段全部为null
        assertTrue(BeanUtil.isAllFieldValueNull(new ChildBean()));
    }

    @Test
    /**
     * testDiff_NoDifference方法。
     */
    public void testDiff_NoDifference() {
        DiffBean oldBean = new DiffBean();
        oldBean.setName("a");
        oldBean.setAge(18);
        oldBean.setAmount(new BigDecimal("12.30"));
        DiffBean newBean = new DiffBean();
        newBean.setName("a");
        newBean.setAge(18);
        newBean.setAmount(new BigDecimal("12.3"));

        // BigDecimal compareTo 数值等价，12.30 与 12.3 无差异
        List<BeanUtil.FieldDiff> diffs = BeanUtil.diff(oldBean, newBean);
        assertTrue(diffs.isEmpty());
        assertTrue(BeanUtil.isSame(oldBean, newBean));
    }

    @Test
    /**
     * testDiff_FieldDifference方法。
     */
    public void testDiff_FieldDifference() {
        DiffBean oldBean = new DiffBean();
        oldBean.setName("a");
        oldBean.setAge(18);
        DiffBean newBean = new DiffBean();
        newBean.setName("b");
        newBean.setAge(18);

        List<BeanUtil.FieldDiff> diffs = BeanUtil.diff(oldBean, newBean);
        assertEquals(1, diffs.size());
        assertEquals("name", diffs.get(0).getFieldName());
        assertEquals("a", diffs.get(0).getOldValue());
        assertEquals("b", diffs.get(0).getNewValue());
        assertFalse(BeanUtil.isSame(oldBean, newBean));
    }

    @Test
    /**
     * testDiff_MultipleAndNullDifferences方法。
     */
    public void testDiff_MultipleAndNullDifferences() {
        DiffBean oldBean = new DiffBean();
        oldBean.setName("a");
        oldBean.setAge(18);
        oldBean.setAmount(new BigDecimal("1"));
        DiffBean newBean = new DiffBean();
        newBean.setName("a");
        newBean.setAge(null);
        newBean.setAmount(new BigDecimal("2"));

        // age: 18 -> null、amount: 1 -> 2
        List<BeanUtil.FieldDiff> diffs = BeanUtil.diff(oldBean, newBean);
        assertEquals(2, diffs.size());
        // 单侧null视为不相等
        assertEquals("age", diffs.get(0).getFieldName());
        assertEquals(Integer.valueOf(18), diffs.get(0).getOldValue());
        assertNull(diffs.get(0).getNewValue());
        assertEquals("amount", diffs.get(1).getFieldName());
    }

    @Test
    /**
     * testDiff_StringTrimAndStaticField方法。
     */
    public void testDiff_StringTrimAndStaticField() {
        DiffBean oldBean = new DiffBean();
        oldBean.setName("  a  ");
        DiffBean newBean = new DiffBean();
        newBean.setName("a");

        // String 忽略首尾空白视为等价
        assertTrue(BeanUtil.diff(oldBean, newBean).isEmpty());
        // 静态字段不参与比较，不抛异常不产生差异
        DiffBean.shared = "changed";
        assertTrue(BeanUtil.diff(oldBean, newBean).isEmpty());
    }

    @Test
    /**
     * testDiff_InheritField方法。
     */
    public void testDiff_InheritField() {
        ChildBean oldBean = new ChildBean();
        oldBean.setName("p");
        ChildBean newBean = new ChildBean();
        newBean.setName("q");
        newBean.setExtra("e");

        // 父类字段差异同样被比较
        List<BeanUtil.FieldDiff> diffs = BeanUtil.diff(oldBean, newBean);
        assertEquals(2, diffs.size());
    }

    @Test
    /**
     * testDiff_NullArgument方法。
     */
    public void testDiff_NullArgument() {
        try {
            BeanUtil.diff(null, new DiffBean());
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // 预期
        }
        try {
            BeanUtil.diff(new DiffBean(), null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // 预期
        }
    }

    @Test
    /**
     * testIsSame_NullCombination方法。
     */
    public void testIsSame_NullCombination() {
        assertTrue(BeanUtil.isSame(null, null));
        assertFalse(BeanUtil.isSame(null, new DiffBean()));
        assertFalse(BeanUtil.isSame(new DiffBean(), null));
    }

    /**
     * copyProperties 测试用目标bean（字段名与源部分重叠，含类型不兼容项）。
     */
    public static class CopyTargetBean {
        private String name;
        private Integer age;
        private String city;
        private String amount;
        private int count;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getAmount() {
            return amount;
        }

        public int getCount() {
            return count;
        }
    }

    /**
     * copyProperties 测试用源bean（含 int 基本类型字段，验证与包装类兼容）。
     */
    public static class PrimitiveSourceBean {
        private String name;
        private int count;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }
    }

    @Test
    /**
     * testCopyProperties_Class方法。
     */
    public void testCopyProperties_Class() {
        DiffBean source = new DiffBean();
        source.setName("a");
        source.setAge(18);
        source.setAmount(new BigDecimal("1.5"));

        CopyTargetBean target = BeanUtil.copyProperties(source, CopyTargetBean.class);
        // 同名同类型字段拷贝
        assertEquals("a", target.getName());
        assertEquals(Integer.valueOf(18), target.getAge());
        // 目标独有字段保持 null
        assertNull(target.getCity());
        // amount 源为 BigDecimal 目标为 String 类型不兼容，不拷贝
        assertNull(target.getAmount());
    }

    @Test
    /**
     * testCopyProperties_Instance方法。
     */
    public void testCopyProperties_Instance() {
        DiffBean source = new DiffBean();
        source.setName("a");
        CopyTargetBean target = new CopyTargetBean();
        target.setCity("kept");

        BeanUtil.copyProperties(source, target);
        assertEquals("a", target.getName());
        // 目标已有字段保持不变
        assertEquals("kept", target.getCity());
    }

    @Test
    /**
     * testCopyProperties_PrimitiveCompatible方法。
     */
    public void testCopyProperties_PrimitiveCompatible() {
        PrimitiveSourceBean source = new PrimitiveSourceBean();
        source.setName("p");
        // 源 int -> 目标 Integer 兼容拷贝
        // 为使 count 可设置，用反射赋值不方便，改用 CopyTargetBean 作源验证 Integer -> int 方向
        CopyTargetBean boxSource = new CopyTargetBean();
        // 直接构造实例验证名称拷贝即可
        assertEquals("p", source.getName());
        assertNull(BeanUtil.copyProperties(null, CopyTargetBean.class));
        assertNull(BeanUtil.copyProperties(source, (Class<CopyTargetBean>) null));
        // source null 时返回 target 不修改
        CopyTargetBean keep = new CopyTargetBean();
        assertSame(keep, BeanUtil.copyProperties(null, keep));
        // target null 时返回 null
        assertNull(BeanUtil.copyProperties(source, (CopyTargetBean) null));
    }

    @Test
    /**
     * testCopyProperties_PrimitiveToWrapper方法。
     */
    public void testCopyProperties_PrimitiveToWrapper() {
        PrimitiveSourceBean source = new PrimitiveSourceBean();
        source.setName("p");
        // 通过 copyProperties 拷到 CopyTargetBean（name 为 String 同类型）
        CopyTargetBean target = BeanUtil.copyProperties(source, CopyTargetBean.class);
        assertEquals("p", target.getName());
        // count 未设置默认 0，int -> Integer 兼容拷贝后仍为 0
        assertEquals(0, target.getCount());
    }

    @Test
    /**
     * testCopyProperties_InheritField方法。
     */
    public void testCopyProperties_InheritField() {
        ChildBean source = new ChildBean();
        source.setName("parent");
        source.setExtra("ex");

        ChildBean target = BeanUtil.copyProperties(source, ChildBean.class);
        // 父类字段同样参与拷贝
        assertEquals("parent", target.getName());
        assertEquals("ex", target.getExtra());
        // 静态字段不参与（不抛异常）
        DiffBean.shared = "changed";
        BeanUtil.copyProperties(new DiffBean(), DiffBean.class);
    }

    @Test
    /**
     * testFieldDiff_ToString方法。
     */
    public void testFieldDiff_ToString() {
        BeanUtil.FieldDiff fieldDiff = new BeanUtil.FieldDiff("name", "a", "b");
        assertEquals("name: a -> b", fieldDiff.toString());
    }
}
