import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

String script = '''/**
 * print top projects by workspaces' size on current node
 */
topCnt = TOP_CNT.toInteger() // how many top jobs will be printed
/**
 * file tree size in bytes
 */
treeSize = {
  it.size() + childSize(it)
}
import java.nio.file.*
/**
 * size of directory's childs
 */ 
childSize = {d ->
  if (d.directory && ! Files.isSymbolicLink(FileSystems.default.getPath(d.path))) {
    d.listFiles().inject(0) {s, f -> 
      s + treeSize(f)
    }
  } else {
    0
  }
}
/**
 * bytes to megabytes
 */
bytes2megs = {a -> 
    Math.ceil(a / (1024 * 1024)).toInteger()
}
// OS Linux
if (WORKSPACE.contains("/")) {
    slash="/"
}
// OS Windows
else {
    slash="\\"
}
//def nodeWCRoot = new File(WORKSPACE).parentFile
def nodeWCRoot = new File(WORKSPACE).parentFile.getParentFile()
println "\nWorkspace root: $nodeWCRoot:"
def freeGB = (bytes2megs(nodeWCRoot.getFreeSpace())/1024).toInteger()
println "\nFree space: $freeGB GB\n"
def lst = []
// If additional file with list of all projects exists
projectslist=new File("$WORKSPACE$slash"+".projectslist")
if (projectslist.exists() && !projectslist.isDirectory()) {
    println "Get list of projects from $projectslist\n"
    projectslist.eachLine {
        (workspace,buildinfo)=it.tokenize('|')
        file=new File(workspace)
        lst.add([bytes2megs(treeSize(file)), buildinfo, file])
    }
}
// Otherwise take everything from root of workspaces as before
else {
    println "Get list of projects from $nodeWCRoot\n"
    nodeWCRoot.eachDir{
	    lst.add([bytes2megs(treeSize(it)), "M-D-YYYY", it.name])
    }
}
def cnt = [topCnt - 1, lst.size() - 1].min()
println "Top $topCnt by workspaces' size:\n\n" + lst.sort {a, b ->
	b[0] - a[0]      
    }[0 .. cnt].collect {
	sprintf('%5d MB \t %s \t %s', *it)
    }.join('\n')''';

String language = "groovy";
System.out.println(hash(script, language));


def bytesToHex(byte[] bytes) {
    final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    def hash(String script, String language) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(language.getBytes("UTF-8"));
            digest.update((byte) ':');
            digest.update(script.getBytes("UTF-8"));
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException x) {
            throw new AssertionError(x);
        }
    }
