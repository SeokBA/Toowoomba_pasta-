package router;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class LayerManager {
    private class _NODE {
        private String token;
        private _NODE next;
        private  _NODE(String input) {
            this.token = input;
            this.next = null;
        }
    }
    private _NODE mp_sListHead;
    private _NODE mp_sListTail;
    private int m_nTop;
    private int m_nLayerCount;
    private ArrayList<BaseLayer> mp_Stack = new ArrayList<>();
    private ArrayList<BaseLayer> mp_aLayers = new ArrayList<>();

    public LayerManager() {
        m_nLayerCount = 0;
        mp_sListHead = null;
        mp_sListTail = null;
        m_nTop = -1;
    }

    public void addLayer(BaseLayer pLayer) {
        mp_aLayers.add(m_nLayerCount++, pLayer);
    }


    public BaseLayer getLayer(int nindex) {
        return mp_aLayers.get(nindex);
    }

    public BaseLayer getLayer(String pName) {
        for (int i = 0; i < m_nLayerCount; i++) {
            if (pName.compareTo(mp_aLayers.get(i).getLayerName()) == 0)
                return mp_aLayers.get(i);
        }
        return null;
    }

    public void connectLayers(String pcList) {
        makeList(pcList);
        linkLayer(mp_sListHead);
    }

    private void makeList(String pcList) {
        StringTokenizer tokens = new StringTokenizer(pcList, " ");
        for (; tokens.hasMoreElements(); ) {
            _NODE pNode = allocNode(tokens.nextToken());
            addNode(pNode);
        }
    }

    private _NODE allocNode(String pcName) {
        _NODE node = new _NODE(pcName);
        return node;
    }

    private void addNode(_NODE pNode) {
        if (mp_sListHead == null) {
            mp_sListHead = mp_sListTail = pNode;
        } else {
            mp_sListTail.next = pNode;
            mp_sListTail = pNode;
        }
    }

    private void push(BaseLayer pLayer) {
        mp_Stack.add(++m_nTop, pLayer);
    }

    private BaseLayer pop() {
        BaseLayer pLayer = mp_Stack.get(m_nTop);
        mp_Stack.remove(m_nTop);
        m_nTop--;

        return pLayer;
    }

    private BaseLayer top() {
        return mp_Stack.get(m_nTop);
    }

    private void linkLayer(_NODE pNode) {
        BaseLayer pLayer = null;
        while (pNode != null) {
            if (pLayer == null)
                pLayer = getLayer(pNode.token);
            else {
                if (pNode.token.equals("("))
                    push(pLayer);
                else if (pNode.token.equals(")"))
                    pop();
                else {
                    char cMode = pNode.token.charAt(0);
                    String pcName = pNode.token.substring(1);
                    pLayer = getLayer(pcName);
                    switch (cMode) {
                        case '*':
                            top().setUpperUnderLayer(pLayer);
                            break;
                        case '+':
                            top().setUpperLayer(pLayer);
                            break;
                        case '-':
                            top().setUnderLayer(pLayer);
                            break;
                    }
                }
            }
            pNode = pNode.next;
        }
    }
}
