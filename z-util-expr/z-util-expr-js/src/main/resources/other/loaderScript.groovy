package other

class LoaderScript {

    def createFile(String fileName) {
        File file = new File(fileName)
        file.createNewFile()
    }
}