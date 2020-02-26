import org.apache.jackrabbit.oak.spi.commit.CommitInfo
import org.apache.jackrabbit.oak.spi.commit.EmptyHook
import org.apache.jackrabbit.oak.spi.state.NodeStore
import org.apache.jackrabbit.oak.commons.PathUtils

def removeNode(def session) {
	def txtfile = new File("output.txt") << new URL ("https://raw.githubusercontent.com/dishantchawla/dcrepository/master/nodelist.txt").getText();
    txtfile.eachLine { line ->
	    println "Entry: ${line}";
	    def path = line.getText(); 	    
    println "Removing node ${path}";

    NodeStore ns = session.store;
    def nb = ns.root.builder();

    def aBuilder = nb;
    for(p in PathUtils.elements(path)) {  
	    aBuilder = aBuilder.getChildNode(p); 
    }

    if(aBuilder.exists()) {
        rm = aBuilder.remove();
        ns.merge(nb, EmptyHook.INSTANCE, CommitInfo.EMPTY);
        return rm;
    } else {
        println "Node ${path} doesn't exist";
        return false;
    }
}

}
	
