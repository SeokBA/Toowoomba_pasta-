package router;
import java.util.ArrayList;

interface BaseLayer {
	int m_nUpperLayerCount = 0;
	String m_pLayerName = null;
	BaseLayer mp_UnderLayer = null;
	ArrayList<BaseLayer> mp_aUpperLayer = new ArrayList<>();

	String getLayerName();
    BaseLayer getUnderLayer(int nindex);
	BaseLayer getUpperLayer(int nindex);
	void setUnderLayer(BaseLayer pUnderLayer);
	void setUpperLayer(BaseLayer pUpperLayer);
	void setUpperUnderLayer(BaseLayer pUULayer);
	default void setUnderUpperLayer(BaseLayer pUULayer) {}
	default boolean send(byte[] input, int length) {
		return false;
	}
	default boolean send(String filename) {
		return false;
	}
	default boolean receive(byte[] input) {
		return false;
	}
	default boolean receive() {
		return false;
	}
}
