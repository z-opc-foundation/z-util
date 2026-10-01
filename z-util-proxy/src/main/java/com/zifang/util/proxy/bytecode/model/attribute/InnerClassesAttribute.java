package com.zifang.util.proxy.bytecode.model.attribute;

import com.zifang.util.proxy.bytecode.model.constantpool.AbstractConstantPool;
import com.zifang.util.proxy.bytecode.model.readtype.U2;
import com.zifang.util.proxy.bytecode.model.readtype.U4;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * InnerClasses 属性（4.7.6）：声明当前类的内部类/嵌套类的关系。
 *  </p>
 * 每条 inner_class_info 是 4 个 u2：
 * inner_class_info_index / outer_class_info_index / inner_name_index /
 * inner_class_access_flags。
 */
public class InnerClassesAttribute extends AbstractAttribute {

    public static final class InnerClassInfo {
        public U2 innerClassInfoIndex;
        public U2 outerClassInfoIndex;
        public U2 innerNameIndex;
        public U2 innerClassAccessFlags;

        public InnerClassInfo(U2 inner, U2 outer, U2 name, U2 access) {
            this.innerClassInfoIndex = inner;
            this.outerClassInfoIndex = outer;
            this.innerNameIndex = name;
            this.innerClassAccessFlags = access;
        }
    }

    private final List<InnerClassInfo> entries = new ArrayList<>();

    public InnerClassesAttribute(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    public void read(InputStream inputStream) {
        U2 count = U2.read(inputStream);
        for (int i = 0; i < count.value; i++) {
            entries.add(new InnerClassInfo(U2.read(inputStream), U2.read(inputStream),
                    U2.read(inputStream), U2.read(inputStream)));
        }
    }

    public List<InnerClassInfo> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}