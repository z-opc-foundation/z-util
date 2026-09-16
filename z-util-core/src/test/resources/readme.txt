一、基本概念介绍

    XPath 是一门在 XML 文档中查找信息的语言， 可用来在 XML 文档中对元素和属性进行遍历。XPath 是 W3C XSLT 标准的主要元素，并且 XQuery 和 XPointer 同时被构建于 XPath 表达之上。因此，对 XPath 的理解是很多高级 XML 应用的基础。
    XPath非常类似对数据库操作的SQL语言，或者说JQuery，它可以方便开发者抓起文档中需要的东西。（dom4j也支持xpath

   1.节点类型

    XPath中有七种结点类型：元素、属性、文本、命名空间、处理指令、注释以及文档节点（或称为根节点）。文档中存在元素节点，属性节点，根节点

   2.常用路径表达式


表达式
描述 
节点名称(nodename)	选取此节点的所有子节点
/	从根节点选取
//	从匹配选择的当前节点选择文档中的节点，而不考虑它们的位置
.	选取当前节点
..	选取当前节点的父节点
@	选取属性
  
示例如下：

//@lang	选取所有名为 lang 的属性
 3.限定语

用来查找某个特定的节点或者包含某个指定的值的节点。以方括号括起

//book[price>35.00]	选择所有book 元素，且其中的 price 元素的值须大于 35.00
/bookstore/book[1]	选取属于 bookstore 子元素的第一个 book 元素。 
/bookstore/book[last()]	选取属于 bookstore 子元素的最后一个 book 元素。 
/bookstore/book[last()-1]	选取属于 bookstore 子元素的倒数第二个 book 元素。 
/bookstore/book[position()<3]	选取最前面的两个属于 bookstore 元素的子元素的 book 元素。 
//title[@lang]	选取所有拥有名为 lang 的属性的 title 元素。 
//title[@lang='eng']	选取所有 title 元素，且这些元素拥有值为 eng 的 lang 属性。 
/bookstore/book[price>35.00]	选取所有 bookstore 元素的 book 元素，且其中的 price 元素的值须大于 35.00。 
/bookstore/book[price>35.00]/title	选取所有 bookstore 元素中的 book 元素的 title 元素，且其中的 price 元素的值须大于 35.00。  4 .通配符
通配符	描述 
*	匹配任何元素节点 
@*	匹配任何属性节点 
node()	匹配任何类型的节点 
|	选取若干路径  
使用示例
路径表达式	结果 
/bookstore/*	选取 bookstore 元素的所有子节点 
//*	选取文档中的所有元素 
//title[@*]	选取所有带有属性的 title 元素。 
//book/title | //book/price	选取所有 book 元素的 tilte 和 price 元素。 
//title | //price	选取所有文档中的 title 和 price 元素。 
/bookstore/book/title | //price	选取所有属于 bookstore 元素的 book 元素的 title 元素，以及文档中所有的 price 元素