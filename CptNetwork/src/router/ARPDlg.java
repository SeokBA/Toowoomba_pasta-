package router;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class ARPDlg extends JFrame implements BaseLayer {
    public int nUpperLayerCount = 0;
    public int nUnderLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<>();
    private byte[] selectedIPAddr={0,0,0,0};
    private byte[] selectedHWAddr={0,0,0,0,0,0};
    Tools tools;


    int adapterNumber = 0;

    Map<String, ARPCacheRecord> cacheTable;
    Map<String, ProxyARPRecord> proxyArpEntry;

    private static LayerManager m_LayerMgr = new LayerManager();

    Container contentPanel;

    JPanel ProxyEnryPanel;
    JPanel ARPCachePanel;
    JPanel gratuitousARPPanel;
    JPanel ARPDisplayPanel;
    JPanel ProxyDisplayPanel;
    JPanel IPAdrPanel;
    JPanel HWAdrPanel;

    JScrollPane scrollARP;
    JScrollPane scrollProxy;

    JButton btnArpItemDelete;
    JButton btnArpAllDelete;
    JButton btnProxyAdd;
    JButton btnProxyDelete;
    JButton btnIpSend;
    JButton btnHWSend;
    JButton btnCancel;
    JButton btnExit;

    JLabel lbIP;
    JLabel lbHW;
    JLabel lbNIC;
    JLabel lbMyIP;
    JLabel lbMyHW;

    DefaultListModel ARPModel;
    DefaultListModel proxyModel;

    JList proxyArea;
    JList ARPArea;

    JTextField tfIPAdr;
    JTextField tfHWAdr;

    static JComboBox<String> NICComboBox;

    PopupProxyAdderDlg popupProxyAdderDlg;

    public static void main(String[] args) throws UnsupportedEncodingException {
        m_LayerMgr.addLayer(new NILayer("NI"));
        m_LayerMgr.addLayer(new EthernetLayer("EtherNet"));
        m_LayerMgr.addLayer(new IPLayer("IP"));
//        m_LayerMgr.addLayer(new TCPLayer("TCP"));
//        m_LayerMgr.addLayer(new ApplicationLayer("Application"));
        m_LayerMgr.addLayer(new ARPLayer("ARP"));
        m_LayerMgr.addLayer(new ARPDlg("GUI"));
//        m_LayerMgr.connectLayers("NI ( *EtherNet ( *IP ( *TCP ( *Application ( *GUI ) ) ) ) *EtherNet ( *ARP ( *GUI ) *ARP ( *IP ) ) )");

//        m_LayerMgr.connectLayers("NI ( *EtherNet ( *IP ( *TCP ( *Application ( *GUI ) ) ) ) *EtherNet ( *ARP ( *GUI ) *ARP ( *IP ) ) )");
        m_LayerMgr.connectLayers("NI ( *EtherNet ( *IP ( *GUI ) ) *EtherNet ( *ARP ( *GUI ) *ARP ( *IP ) ) )");

        System.out.println(m_LayerMgr.getLayer("NI").getUpperLayer(0));
        System.out.println(m_LayerMgr.getLayer("EtherNet").getUpperLayer(0));
        System.out.println(m_LayerMgr.getLayer("EtherNet").getUpperLayer(1));
        System.out.println(m_LayerMgr.getLayer("EtherNet").getUnderLayer(0));
        System.out.println(m_LayerMgr.getLayer("IP").getUnderLayer(1)); // ip레이어의 under1번이 arp
        System.out.println(m_LayerMgr.getLayer("IP").getUnderLayer(0));
        System.out.println(m_LayerMgr.getLayer("ARP").getUnderLayer(0));
        System.out.println("dd"+m_LayerMgr.getLayer("ARP").getUpperLayer(0));
        System.out.println("dd"+m_LayerMgr.getLayer("ARP").getUpperLayer(1));

        System.out.println(m_LayerMgr.getLayer("GUI").getUnderLayer(0));
        System.out.println(m_LayerMgr.getLayer("EtherNet").getUnderLayer(0));
        System.out.println(m_LayerMgr.getLayer("IP").getUnderLayer(0));
//        System.out.println(m_LayerMgr.getLayer("TCP").getUnderLayer(0));

        System.out.println(m_LayerMgr.getLayer("GUI").getUnderLayer(0));
        System.out.println(m_LayerMgr.getLayer("NI").getUpperLayer(0));
        System.out.println(m_LayerMgr.getLayer("NI").getUpperLayer(0));
    }

    public ARPDlg(String pName) throws UnsupportedEncodingException {
        tools =new Tools();
        pLayerName = pName;
        proxyArpEntry=ProxyARPTable.getInstance().getProxyArpEntry();
        cacheTable=ARPCacheTable.getInstance().getCacheTable();
        setTitle("TestARP"); // main
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250, 250, 797, 460);
        contentPanel = new JPanel();
        ((JComponent) contentPanel).setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPanel);
        contentPanel.setLayout(null);


        // Panel
        ARPCachePanel = new JPanel(); // ARP Enrty panel
        ARPCachePanel.setLayout(null);
        ARPCachePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ARP Cache", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        ARPCachePanel.setBounds(20, 42, 360, 325);
        contentPanel.add(ARPCachePanel);

        ProxyEnryPanel = new JPanel(); // Proxy Enrty panel
        ProxyEnryPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Proxy ARP Entry", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        ProxyEnryPanel.setBounds(394, 42, 360, 234);
        contentPanel.add(ProxyEnryPanel);
        ProxyEnryPanel.setLayout(null);

        gratuitousARPPanel = new JPanel(); // gratuitous Enrty panel
        gratuitousARPPanel.setLayout(null);
        gratuitousARPPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gratuitous ARP", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        gratuitousARPPanel.setBounds(394, 277, 360, 90);
        contentPanel.add(gratuitousARPPanel);

        ARPDisplayPanel = new JPanel(); // ARP display area panel
        ARPDisplayPanel.setLayout(null);
        ARPDisplayPanel.setBounds(10, 15, 340, 210);
        ARPCachePanel.add(ARPDisplayPanel);

        ProxyDisplayPanel = new JPanel(); // Proxy display area panel
        ProxyDisplayPanel.setBounds(10, 15, 340, 167);
        ProxyEnryPanel.add(ProxyDisplayPanel);
        ProxyDisplayPanel.setLayout(null);

        IPAdrPanel = new JPanel(); // IP address text field panel
        IPAdrPanel.setLayout(null);
        IPAdrPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        IPAdrPanel.setBounds(50, 276, 195, 20);
        ARPCachePanel.add(IPAdrPanel);

        HWAdrPanel = new JPanel(); // HW address text field panel
        HWAdrPanel.setLayout(null);
        HWAdrPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        HWAdrPanel.setBounds(56, 42, 195, 20);
        gratuitousARPPanel.add(HWAdrPanel);


        // Scroll panel
        scrollARP = new JScrollPane();
        scrollARP.setBounds(0, 0, 340, 210);
        ARPDisplayPanel.add(scrollARP);

        scrollProxy = new JScrollPane();
        scrollProxy.setBounds(0, 0, 340, 210);
        ProxyDisplayPanel.add(scrollProxy);


        // Button
        btnArpItemDelete = new JButton("Item Delete");
        btnArpItemDelete.setBounds(34, 237, 130, 27);
        btnArpItemDelete.addActionListener(new setAddressListener());
        ARPCachePanel.add(btnArpItemDelete);

        btnArpAllDelete = new JButton("All Delete");
        btnArpAllDelete.setBounds(198, 237, 130, 27);
        btnArpAllDelete.addActionListener(new setAddressListener());
        ARPCachePanel.add(btnArpAllDelete);

        btnProxyAdd = new JButton("Add");
        btnProxyAdd.setBounds(34, 194, 130, 27);
        btnProxyAdd.addActionListener(new setAddressListener());
        ProxyEnryPanel.add(btnProxyAdd);

        btnProxyDelete = new JButton("Delete");
        btnProxyDelete.setBounds(194, 194, 130, 27);
        btnProxyDelete.addActionListener(new setAddressListener());
        ProxyEnryPanel.add(btnProxyDelete);

        btnIpSend = new JButton("Send");
        btnIpSend.setBounds(253, 276, 80, 20);
        btnIpSend.addActionListener(new setAddressListener());
        ARPCachePanel.add(btnIpSend);

        btnHWSend = new JButton("Send");
        btnHWSend.setBounds(265, 42, 80, 20);
        btnHWSend.addActionListener(new setAddressListener());
        gratuitousARPPanel.add(btnHWSend);

        btnCancel = new JButton("취소");
        btnCancel.setBounds(394, 379, 99, 27);
        btnCancel.addActionListener(new setAddressListener());
        contentPanel.add(btnCancel);

        btnExit = new JButton("종료");
        btnExit.setBounds(281, 379, 99, 27);
        btnExit.addActionListener(new setAddressListener());
        contentPanel.add(btnExit);


        // Label
        lbIP = new JLabel();
        lbIP.setText("IP");
        lbIP.setBounds(24, 272, 29, 24);
        ARPCachePanel.add(lbIP);

        lbHW = new JLabel();
        lbHW.setText("H/W");
        lbHW.setBounds(14, 38, 45, 24);
        gratuitousARPPanel.add(lbHW);

        lbNIC = new JLabel("NIC 선택");
        lbNIC.setBounds(20, 15, 50, 20);
        contentPanel.add(lbNIC);

        lbMyIP = new JLabel("My IP Address : ");
        lbMyIP.setBounds(280, 8, 400, 20);
        contentPanel.add(lbMyIP);

        lbMyHW = new JLabel("My Mac Address : ");
        lbMyHW.setBounds(280, 22, 400, 20);
        contentPanel.add(lbMyHW);



        // List Model
        ARPModel = new DefaultListModel();
        proxyModel = new DefaultListModel();


        // List area
        ARPArea = new JList(ARPModel);
        scrollARP.setViewportView(ARPArea);
        proxyArea = new JList(proxyModel);
        scrollProxy.setViewportView(proxyArea);

        // Text field
        tfIPAdr = new JTextField(); // IP address text field
        tfIPAdr.setColumns(10);
        tfIPAdr.setBounds(0, 0, 195, 24);
        IPAdrPanel.add(tfIPAdr);

        tfHWAdr = new JTextField(); // HW address text field
        tfHWAdr.setColumns(10);
        tfHWAdr.setBounds(0, 0, 195, 24);
        HWAdrPanel.add(tfHWAdr);

        List<String> adapterList=((NILayer) m_LayerMgr.getLayer("NI")).getAdapterList();
        NICComboBox = new JComboBox(adapterList.toArray(new String[adapterList.size()]) );
        NICComboBox.setBounds(70, 15, 200, 20);
//        NICComboBox.addActionListener(new setAddressListener());
        contentPanel.add(NICComboBox);

        // 이 부분에서 ip, mac주소 세팅
        NICComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                Object obj = ev.getItem();
                for (int i = 0; i < adapterList.size(); i++) {
                    if (obj.equals(((NILayer) m_LayerMgr.getLayer("NI")).getDescription(i))) {
                        adapterNumber = i;
                        try {
                            // 자신의 맥주소 가져옴
                            byte[] macAddress = ((NILayer) m_LayerMgr.getLayer("NI")).getMacAddress(i);
                            ((EthernetLayer) m_LayerMgr.getLayer("EtherNet")).setEnetSrcAddress(macAddress);
                            ((ARPLayer) m_LayerMgr.getLayer("ARP")).setSrcHWAddress(macAddress);
                            String macAddressStr=tools.hwAddrByte2String(macAddress, '-');
                            for(int k=0; k<macAddress.length; k++){
                                selectedHWAddr[k]=macAddress[k];
                            }
                            byte[] ipAddress = ((NILayer) m_LayerMgr.getLayer("NI")).getIpAddress(i);
                            if(ipAddress.length==4){
                                for(int k=0; k<4; k++){
                                    selectedIPAddr[k]=ipAddress[k];
                                }
                                ((IPLayer) m_LayerMgr.getLayer("IP")).setSrcAddress(ipAddress);
                                ((ARPLayer) m_LayerMgr.getLayer("ARP")).setSrcPTAddress(ipAddress);
                            }
                            String ipAddressStr=tools.ipAddrByte2String(ipAddress);
                            lbMyIP.setText("My IP Address : "+ipAddressStr);
                            lbMyHW.setText("My Mac Address : "+macAddressStr);
                            ((NILayer) m_LayerMgr.getLayer("NI")).setAdapterNumber(adapterNumber);
                            } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        );

        setVisible(true);
    }

    public class PopupProxyAdderDlg extends JFrame{
        JPanel addDevicePanel;
        JPanel addProxyIPPanel;
        JPanel addProxyEtherPanel;

        JButton btnOK;
        JButton btnCancel;

        JLabel lbSelectDevice;
        JLabel lbAddProxyIP;
        JLabel lbAddProxyEther;

        JComboBox<String> diviceSelectBox;

        JTextField tfAddProxyIP;
        JTextField tfAddProxyEther;

        public PopupProxyAdderDlg() {
            // main
            setTitle("Proxy ARP Entry 추가");
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            setSize(320,220);
            setLayout(null);


            // Panel
            addDevicePanel = new JPanel();
            addDevicePanel.setLayout(null);
            addDevicePanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
            addDevicePanel.setBounds(130, 23, 150, 20);
            add(addDevicePanel);

            addProxyIPPanel = new JPanel();
            addProxyIPPanel.setLayout(null);
            addProxyIPPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
            addProxyIPPanel.setBounds(130, 63, 150, 20);
            add(addProxyIPPanel);

            addProxyEtherPanel = new JPanel();
            addProxyEtherPanel.setLayout(null);
            addProxyEtherPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
            addProxyEtherPanel.setBounds(130, 103, 150, 20);
            add(addProxyEtherPanel);


            // Button
            btnOK = new JButton("OK");
            btnOK.setBounds(50, 140, 100, 27);
            btnOK.addActionListener(new setPopupListener());
            add(btnOK);

            btnCancel = new JButton("Cancel");
            btnCancel.setBounds(160, 140, 100, 27);
            btnCancel.addActionListener(new setPopupListener());
            add(btnCancel);


            // Label
            lbSelectDevice = new JLabel("Device");
            lbSelectDevice.setBounds(20, 20, 130, 24);
            add(lbSelectDevice);

            lbAddProxyIP = new JLabel("IP 주소");
            lbAddProxyIP.setBounds(20, 60, 130, 24);
            add(lbAddProxyIP);

            lbAddProxyEther = new JLabel("Ethernet 주소");
            lbAddProxyEther.setBounds(20, 100, 130, 24);
            add(lbAddProxyEther);


            // ComboBox
            String[] hostArray={"Host A", "Host B", "Host C", "Host D"};
            diviceSelectBox = new JComboBox<>(hostArray);
            diviceSelectBox.setBounds(0, 0, 150, 20);
            diviceSelectBox.addActionListener(new setPopupListener());
            addDevicePanel.add(diviceSelectBox);


            // Text field
            tfAddProxyIP = new JTextField();
            tfAddProxyIP.setBounds(0, 0, 170, 20);
            tfAddProxyIP.setColumns(10);
            addProxyIPPanel.add(tfAddProxyIP);
            add(addProxyIPPanel);

            tfAddProxyEther = new JTextField();
            tfAddProxyEther.setBounds(0, 0, 170, 20);
            tfAddProxyEther.setColumns(10);
            addProxyEtherPanel.add(tfAddProxyEther);
            add(addProxyEtherPanel);
        }

        class setPopupListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btnOK) { // Pushed "OK" button
                    String hostID=diviceSelectBox.getSelectedItem().toString();
                    ProxyARPRecord proxyArpData=new ProxyARPRecord(tfAddProxyIP.getText(),tfAddProxyEther.getText());
                    proxyArpEntry.put(hostID, proxyArpData);
                    updateProxyArpEntry();
                }

                if (e.getSource() == btnCancel) { // Pushed "Cancel" button
                    setVisible(false);
                }
            }
        }
    }

    class setAddressListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnArpItemDelete) { // Pushed "Item Delete" button
                int index = ARPArea.getSelectedIndex();
                String selectedItem=ARPModel.get(index).toString();
                String[] split=selectedItem.split(" ");
                String ipAddr=split[0].trim();
                cacheTable.remove(ipAddr);
                ARPModel.remove(index);
            }
            if (e.getSource() == btnArpAllDelete) { // Pushed "All Delete" button
                cacheTable.clear();
                updateCacheTable();
            }
            if (e.getSource() == btnProxyAdd) { // Pushed "Add" button (Popup Proxy Adder Dialog)
                if (popupProxyAdderDlg == null)
                    popupProxyAdderDlg = new PopupProxyAdderDlg();
                popupProxyAdderDlg.setVisible(true);
            }
            if (e.getSource() == btnProxyDelete) { // Pushed "Delete" button
                int index = proxyArea.getSelectedIndex();
                String selectedItem=proxyModel.get(index).toString();
                // 키가 Host A,B,C,D 무조건 이렇게 생겼다고 가정
                String hostID=selectedItem.substring(0,6);
                System.out.println(hostID);
                proxyArpEntry.remove(hostID);
                proxyModel.remove(index);
            }
            if (e.getSource() == btnIpSend) { // Pushed "Send" button in ARP Cache Panel
                cacheTable=ARPCacheTable.getInstance().getCacheTable();
                String findIP=tfIPAdr.getText();
                cacheTable.put(findIP,new ARPCacheRecord("????????", "Incomplete"));
                updateCacheTable();
                ((EthernetLayer) m_LayerMgr.getLayer("EtherNet")).setEnetSrcAddress(selectedHWAddr);
                ((ARPLayer) m_LayerMgr.getLayer("ARP")).setSrcHWAddress(selectedHWAddr);
                // 입력받은 아이피 byte형으로 변환
                byte[] data=findIP.getBytes();
                m_LayerMgr.getLayer("IP").send(data,data.length);
            }
            if (e.getSource() == btnHWSend) { // Pushed "Send" button in Gratuitous ARP Panel
//                ARPModel.addElement("send for gArp");
                String gArp=tfHWAdr.getText();
                byte[] data=gArp.getBytes();
                byte[] hwAddr= tools.stringHWaddrToByte(gArp);
                ((EthernetLayer) m_LayerMgr.getLayer("EtherNet")).setEnetSrcAddress(hwAddr);
                ((ARPLayer) m_LayerMgr.getLayer("ARP")).setSrcHWAddress(hwAddr);
                ((IPLayer) m_LayerMgr.getLayer("IP")).setDstAddress(selectedIPAddr);
//                    m_ArpLayer.setSrcHWAddress(stringHWaddrToByte(gArp));
                // todo underlayer로 변환 필요
                m_LayerMgr.getLayer("IP").send(data,data.length);
            }
            if (e.getSource() == btnCancel) { // Pushed "취소" button
                // todo chatting까지 연결되면 쓸 부분
                System.out.println("cancel button pressed");
            }
            if (e.getSource() == btnExit) { // Pushed "종료" button
                System.exit(0);
            }
        }
    }

    public void updateProxyArpEntry(){
        cacheTable=ARPCacheTable.getInstance().getCacheTable();
        proxyModel.clear();
        proxyArpEntry.forEach((k,v) -> proxyModel.addElement(k+"  "+v.hostIpAddr+"  "+v.routerMacAddr+""));
    }

    public boolean receive(byte[] input) {
        String s;
        s = new String(input);
        return true;
    }

    public void updateCacheTable(){
        ARPModel.clear();
        cacheTable.forEach((k,v) -> ARPModel.addElement(k+"  "+v.hardwareAddr+"  "+v.status+""));
    }

    @Override
    public void setUnderLayer(BaseLayer pUnderLayer) {
        if (pUnderLayer == null)
            return;
        this.p_aUnderLayer.add(nUnderLayerCount++, pUnderLayer);
    }

    @Override
    public void setUpperLayer(BaseLayer pUpperLayer) {
        if (pUpperLayer == null)
            return;
        this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
        // nUpperLayerCount++;
    }

    @Override
    public String getLayerName() {
        return pLayerName;
    }

    @Override
    public BaseLayer getUnderLayer(int nindex) {
        if (nindex < 0 || nindex > nUnderLayerCount || nUnderLayerCount < 0)
            return null;
        return p_aUnderLayer.get(nindex);
    }

    @Override
    public BaseLayer getUpperLayer(int nindex) {
        if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
            return null;
        return p_aUpperLayer.get(nindex);
    }

    @Override
    public void setUpperUnderLayer(BaseLayer pUULayer) {
        this.setUpperLayer(pUULayer);
        pUULayer.setUnderLayer(this);
    }
}