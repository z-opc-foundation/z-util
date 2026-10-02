package other
/**
 * Groovy容器
 */
class GroovyCabin {
}

/*
 * List 有丰富的api idea中可用listA. 查看
 */
def listA = [5, "string", "false"]
print listA
println listA[1]
println listA.size()
listA.add("moniter")
println listA.size()
listA[9] = 9
println listA
listA << 7   //在listA里添加数据 相当于listA.add(7)
println listA

/*
 * Map
 */

def map = [key1: "john", key2: "tom", key3: listA]
println map.keySet()
println map.values()
println map.get("key2") //返回key2的值
map.put("key4", "company")
println map
//遍历
Iterator it = map.iterator()
while (it.hasNext()) {
    println it.next()
}
println map.containsValue("tom")
println map.containsKey("key1")
Set set = map.keySet()  //把map的Key值转换为set
println set

/*
 * 闭包 Clouser 闭包是一种数据类型，它代表了一段可执行的代码
 *
 def xx = {
     params -> code
 }

  def xx = {
     code
 }

 */

def clouserA = {
    String param1, int param2 ->
        println "this is code"
        return param1 + "welcome" + param2
}
/*
 * 闭包定义好后，要调用它的方法就是 闭包对象.call(参数)  or 闭包对象（参数）
 */
println clouserA.call("this is code", 100)
println clouserA("A", 200)


def funC = {
    -> "abced"
}
println funC.call()

def list = [1, 2, 3] //定义一个list
list.each {
    println it
}

//当函数的最后一个参数是闭包的话，可以省略圆括号

def funD(int num, String str, Closure closure) {
    println num + str
    closure() //调用闭包
}

funD(4, "test", {
    println "close package"
})