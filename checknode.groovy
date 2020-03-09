import org.apache.jackrabbit.oak.spi.commit.CommitInfo
import org.apache.jackrabbit.oak.spi.commit.EmptyHook
import org.apache.jackrabbit.oak.spi.state.NodeStore
import org.apache.jackrabbit.oak.commons.PathUtils

def checknode(def session, def line) {
	
    
		println "Entry: ${line}";
		println "Removing node ${line}";

		NodeStore ns = session.store;
		def nb = ns.root.builder();

		def aBuilder = nb;
		for(p in PathUtils.elements(line)) {  
			aBuilder = aBuilder.getChildNode(p); 
		}

		if(aBuilder.exists()) {
		println "Exists: ${aBuilder}";
			//rm = aBuilder.remove();
			//ns.merge(nb, EmptyHook.INSTANCE, CommitInfo.EMPTY);
			//return rm;
		} else {
			println "Node ${line} doesn't exist";
			return false;
		}
	
}
	