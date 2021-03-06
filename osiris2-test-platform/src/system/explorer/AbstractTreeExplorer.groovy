package system.explorer;

import com.rameses.rcp.common.*;
import com.rameses.rcp.annotations.*;
import com.rameses.osiris2.client.*;


public abstract class AbstractTreeExplorer {

    def selection;
    def rootNode;
    def defaultIcon = 'images/doc-view16.png';
                
    public abstract void init();

    void doInit( root ) {
        loadChildren( root )
        rootNode = new Node(id:root.id, caption:root.caption, item: root); 
    }

    boolean loadChildren( root ) {
        def folders = OsirisContext.session.getFolders( root.folder ? root.folder : root.id );
        if( !folders ) return false;

        root.children = [];
        Iterator itr = folders.iterator();
        while( itr.hasNext() ) {
            def f = itr.next();
            def item = [id: f.id, caption: f.caption, folder: f];

            if( f.invoker || loadChildren(item) ) {
                root.children.add(item)
            }
        }

        return root.children.size() > 0
    }

    def tree = [
        getRootNode : { 
            return rootNode;
        },

        fetchNodes : { o->
            if( !o.item ) return null;

            def nodes = []
            def folders = o.item.children
            folders.each {
                def f = it.folder
                if ( f.invoker )
                    nodes << new Node(
                        id: f.invoker.workunitid, caption: f.invoker.caption, 
                        item: f.invoker, leaf: true,
                        icon: f.invoker.properties.icon ? f.invoker.properties.icon : defaultIcon
                    );
                else
                    nodes << new Node(id: it.id, caption: it.caption, item: it);
            }
            return nodes;
        },

        openLeaf: { n ->
            if( n.item ) {
                InvokerUtil.invoke( n.item );
            }    
        }
    ] as TreeNodeModel;
}