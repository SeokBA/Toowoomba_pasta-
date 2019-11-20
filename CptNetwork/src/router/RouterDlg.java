package router;

import org.jnetpcap.PcapIf;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;

public class RouterDlg extends JFrame implements BaseLayer {
    public int nUpperLayerCount = 0;
    public int nUnderLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<>();
    BaseLayer UnderLayer;

    RoutingTable routingTable = new RoutingTable();
    ARPCacheTable arpCacheTable = new ARPCacheTable();
    ProxyARPTable proxyArpTable = new ProxyARPTable();

    private static LayerManager m_LayerMgr = new LayerManager();

    String[] staticRoutingHeader = {"Destination", "NetMask", "Gateway", "Flag", "Interface", "Metric"};
    String[] arpHeader = {"IP Address", "Ethernet Address", "Interface", "Flag"};
    String[] proxyHeader = {"IP Address", "Ethernet Address", "Interface"};
    String[][] staticRoutingTable = {};
    String[][] arpTable = {};
    String[][] proxyTable = {};

    Container contentPanel;

    JPanel StaticRoutingPanel;
    JPanel ARPCachePanel;
    JPanel ProxyARPPanel;
    JPanel StaticRoutingDisplayPanel;
    JPanel ARPCacheDisplayPanel;
    JPanel ProxyARPDisplayPanel;

    JScrollPane scrollARP;
    JScrollPane scrollProxy;
    JScrollPane scrollRouting;

    JButton btnRoutingAdd;
    JButton btnRoutingDelete;
    JButton btnARPDelete;
    JButton btnProxyAdd;
    JButton btnProxyDelete;

    JTable staticRoutingArea;
    JTable ARPArea;
    JTable proxyArea;

    PopupRoutingAdderDlg popupRoutingAdderDlg;

    JLabel lbLeftHandIP;
    JLabel lbRightHandIP;
    JLabel lbLeftHandMAC;
    JLabel lbRightHandMAC;

    setAddressListener setAddressListener = new setAddressListener();
    setMouseListener setMouseListener = new setMouseListener();

    Tools tools = new Tools();

    public static void main(String[] args) {
        m_LayerMgr.addLayer(new NILayer("NI"));
        m_LayerMgr.addLayer(new EthernetLayer("EtherNet"));
        m_LayerMgr.addLayer(new IPLayer("IP"));
        m_LayerMgr.addLayer(new ARPLayer("ARP"));
        m_LayerMgr.addLayer(new RouterDlg("GUI"));
        m_LayerMgr.connectLayers("NI ( *EtherNet ( *IP ( *GUI ) ) *EtherNet ( *ARP ( *GUI ) *ARP ( *IP ) ) )");
    }

    public RouterDlg(String pName) {
        pLayerName = pName;

        // main
        setTitle("TestRouting");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250, 250, 980, 460);
        contentPanel = new JPanel();
        ((JComponent) contentPanel).setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPanel);
        contentPanel.setLayout(null);


        // Panel
        StaticRoutingPanel = new JPanel();
        StaticRoutingPanel.setLayout(null);
        StaticRoutingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Static Routing Table", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        StaticRoutingPanel.setBounds(20, 60, 500, 345);
        contentPanel.add(StaticRoutingPanel);

        ARPCachePanel = new JPanel();
        ARPCachePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ARP Cache Table", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        ARPCachePanel.setBounds(540, 60, 400, 170);
        contentPanel.add(ARPCachePanel);
        ARPCachePanel.setLayout(null);

        ProxyARPPanel = new JPanel();
        ProxyARPPanel.setLayout(null);
        ProxyARPPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Proxy ARP Table", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        ProxyARPPanel.setBounds(540, 235, 400, 170);
        contentPanel.add(ProxyARPPanel);

        StaticRoutingDisplayPanel = new JPanel();
        StaticRoutingDisplayPanel.setLayout(null);
        StaticRoutingDisplayPanel.setBounds(10, 15, 480, 300);
        StaticRoutingPanel.add(StaticRoutingDisplayPanel);

        ARPCacheDisplayPanel = new JPanel();
        ARPCacheDisplayPanel.setBounds(10, 15, 380, 125);
        ARPCachePanel.add(ARPCacheDisplayPanel);
        ARPCacheDisplayPanel.setLayout(null);

        ProxyARPDisplayPanel = new JPanel();
        ProxyARPDisplayPanel.setBounds(10, 15, 380, 125);
        ProxyARPPanel.add(ProxyARPDisplayPanel);
        ProxyARPDisplayPanel.setLayout(null);


        // Scroll panel
        scrollRouting = new JScrollPane();
        scrollRouting.setBounds(0, 0, 480, 300);
        StaticRoutingDisplayPanel.add(scrollRouting);

        scrollARP = new JScrollPane();
        scrollARP.setBounds(0, 0, 380, 125);
        ARPCacheDisplayPanel.add(scrollARP);

        scrollProxy = new JScrollPane();
        scrollProxy.setBounds(0, 0, 380, 125);
        ProxyARPDisplayPanel.add(scrollProxy);


        // Button
        btnRoutingAdd = new JButton("Add");
        btnRoutingAdd.setBounds(104, 315, 120, 25);
        btnRoutingAdd.addActionListener(setAddressListener);
        StaticRoutingPanel.add(btnRoutingAdd);

        btnRoutingDelete = new JButton("Delete");
        btnRoutingDelete.setBounds(268, 315, 120, 25);
        btnRoutingDelete.addActionListener(setAddressListener);
        StaticRoutingPanel.add(btnRoutingDelete);

        btnARPDelete = new JButton("Delete");
        btnARPDelete.setBounds(140, 140, 120, 25);
        btnARPDelete.addActionListener(setAddressListener);
        ARPCachePanel.add(btnARPDelete);

        btnProxyAdd = new JButton("Add");
        btnProxyAdd.setBounds(54, 140, 120, 25);
        btnProxyAdd.addActionListener(setAddressListener);
        ProxyARPPanel.add(btnProxyAdd);

        btnProxyDelete = new JButton("Delete");
        btnProxyDelete.setBounds(218, 140, 120, 25);
        btnProxyDelete.addActionListener(setAddressListener);
        ProxyARPPanel.add(btnProxyDelete);


        // List area
        staticRoutingArea = new JTable(staticRoutingTable, staticRoutingHeader);
        scrollRouting.setViewportView(staticRoutingArea);

        ARPArea = new JTable(arpTable, arpHeader);
        scrollARP.setViewportView(ARPArea);

        proxyArea = new JTable(proxyTable, proxyHeader);
        scrollProxy.setViewportView(proxyArea);


        // Label
        lbLeftHandIP = new JLabel("LeftHand IP      : ");
        lbLeftHandIP.setBounds(20, 10, 260, 25);
        lbLeftHandIP.addMouseListener(setMouseListener);
        contentPanel.add(lbLeftHandIP);

        lbLeftHandMAC = new JLabel("LeftHand MAC : ");
        lbLeftHandMAC.setBounds(20, 30, 260, 25);
        lbLeftHandMAC.addMouseListener(setMouseListener);
        contentPanel.add(lbLeftHandMAC);

        lbRightHandIP = new JLabel("RightHand IP      : ");
        lbRightHandIP.setBounds(700, 10, 260, 25);
        lbRightHandIP.addMouseListener(setMouseListener);
        contentPanel.add(lbRightHandIP);

        lbRightHandMAC = new JLabel("RightHand MAC : ");
        lbRightHandMAC.setBounds(700, 30, 260, 25);
        lbRightHandMAC.addMouseListener(setMouseListener);
        contentPanel.add(lbRightHandMAC);


        setVisible(true);
    }

    class setAddressListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) { // routing add
            if (e.getSource() == btnRoutingAdd) {
                if (popupRoutingAdderDlg == null)
                    popupRoutingAdderDlg = new PopupRoutingAdderDlg((NILayer) m_LayerMgr.getLayer("NI"));
                popupRoutingAdderDlg.updateInterface();
                popupRoutingAdderDlg.setVisible(true);
            }
            if (e.getSource() == btnRoutingDelete) { // routing delete

            }
            if (e.getSource() == btnARPDelete) { // arp delete

            }
            if (e.getSource() == btnProxyAdd) { // proxy add

            }
            if (e.getSource() == btnProxyDelete) { // proxy delete

            }
        }


    }

    class setMouseListener implements MouseListener {
        PopupSelectNICDlg popupSelectNICDlg = new PopupSelectNICDlg((NILayer) m_LayerMgr.getLayer("NI"));

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() == lbLeftHandIP || e.getSource() == lbLeftHandMAC) {
                popupSelectNICDlg.popup("Left NIC", false);
            }

            if (e.getSource() == lbRightHandIP || e.getSource() == lbRightHandMAC) {
                popupSelectNICDlg.popup("Right NIC", true);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    public class PopupSelectNICDlg extends JFrame {
        NILayer m_NILayer;

        JButton btnOK;
        JButton btnCancel;

        JComboBox<String> addInterfaceComboBox;

        setPopupListener setPopupListener = new setPopupListener();

        boolean handChk;

        public PopupSelectNICDlg(NILayer m_NILayer) {
            this.m_NILayer = m_NILayer;

            // main
            setTitle("Select NIC");
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            setBounds(250, 250, 270, 140);
            setLayout(null);

            // Button
            btnOK = new JButton("OK");
            btnOK.setBounds(20, 60, 100, 27);
            btnOK.addActionListener(setPopupListener);
            add(btnOK);

            btnCancel = new JButton("Cancel");
            btnCancel.setBounds(140, 60, 100, 27);
            btnCancel.addActionListener(setPopupListener);
            add(btnCancel);

            // ComboBox
            addInterfaceComboBox = new JComboBox<>();
            addInterfaceComboBox.setBounds(30, 20, 190, 24);
            add(addInterfaceComboBox);
        }

        public void popup(String title, boolean handChk) {
            setTitle(title);
            this.handChk = handChk;
            addInterfaceComboBox.removeAllItems();
            for (int i = 0; i < m_NILayer.m_pAdapterList.size(); i++) {
                addInterfaceComboBox.addItem(m_NILayer.m_pAdapterList.get(i).getDescription());
            }
            setVisible(true);
        }

        class setPopupListener implements ActionListener {
            String ipAddressStr = "", macAddressStr = "";
            int selected;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btnOK) { // Pushed "OK" button
                    selected = addInterfaceComboBox.getSelectedIndex();
                    ipAddressStr = tools.ipAddrByteToString(m_NILayer.getIpAddress(selected));
                    try {
                        macAddressStr = tools.hwAddrByteToString(m_NILayer.getMacAddress(selected), ':');
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    if (!handChk) {
                        lbLeftHandIP.setText("LeftHand IP      : " + ipAddressStr);
                        lbLeftHandMAC.setText("LeftHand MAC : " + macAddressStr);
                    } else {
                        lbRightHandIP.setText("LeftHand IP      : " + ipAddressStr);
                        lbRightHandMAC.setText("LeftHand MAC : " + macAddressStr);
                    }
                    setVisible(false);
                }

                if (e.getSource() == btnCancel) { // Pushed "Cancel" button
                    setVisible(false);
                }
            }
        }
    }

    public class PopupRoutingAdderDlg extends JFrame {
        NILayer m_NILayer;

        JPanel addDestinationPanel;
        JPanel addNetmaskPanel;
        JPanel addGatewayPanel;

        JButton btnOK;
        JButton btnCancel;

        JLabel lbDestination;
        JLabel lbNetmask;
        JLabel lbGateway;
        JLabel lbFlag;
        JLabel lbInterface;

        JTextField tfDestination;
        JTextField tfNetmask;
        JTextField tfGateway;

        JCheckBox chkUpFlag;
        JCheckBox chkGatewayFlag;
        JCheckBox chkHostFlag;

        JComboBox<String> addInterfaceComboBox;

        setPopupListener setPopupListener = new setPopupListener();

        public PopupRoutingAdderDlg(NILayer m_NILayer) {
            this.m_NILayer = m_NILayer;

            // main
            setTitle("Routing Table Entry");
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            setSize(330, 250);
            setLayout(null);


            // Panel
            addDestinationPanel = new JPanel();
            addDestinationPanel.setLayout(null);
            addDestinationPanel.setBounds(110, 10, 190, 24);
            add(addDestinationPanel);

            addNetmaskPanel = new JPanel();
            addNetmaskPanel.setLayout(null);
            addNetmaskPanel.setBounds(110, 40, 190, 24);
            add(addNetmaskPanel);

            addGatewayPanel = new JPanel();
            addGatewayPanel.setLayout(null);
            addGatewayPanel.setBounds(110, 70, 190, 24);
            add(addGatewayPanel);


            // Button
            btnOK = new JButton("OK");
            btnOK.setBounds(50, 165, 100, 27);
            btnOK.addActionListener(setPopupListener);
            add(btnOK);

            btnCancel = new JButton("Cancel");
            btnCancel.setBounds(160, 165, 100, 27);
            btnCancel.addActionListener(setPopupListener);
            add(btnCancel);


            // Label
            lbDestination = new JLabel("Destination");
            lbDestination.setBounds(20, 10, 130, 24);
            add(lbDestination);

            lbNetmask = new JLabel("Netmask");
            lbNetmask.setBounds(20, 40, 130, 24);
            add(lbNetmask);

            lbGateway = new JLabel("Gateway");
            lbGateway.setBounds(20, 70, 130, 24);
            add(lbGateway);

            lbFlag = new JLabel("Flag");
            lbFlag.setBounds(20, 100, 130, 24);
            add(lbFlag);

            lbInterface = new JLabel("Interface");
            lbInterface.setBounds(20, 130, 130, 24);
            add(lbInterface);


            // Text field
            tfDestination = new JTextField();
            tfDestination.setBounds(0, 0, 190, 24);
            tfDestination.setColumns(10);
            addDestinationPanel.add(tfDestination);

            tfNetmask = new JTextField();
            tfNetmask.setBounds(0, 0, 190, 24);
            tfNetmask.setColumns(10);
            addNetmaskPanel.add(tfNetmask);

            tfGateway = new JTextField();
            tfGateway.setBounds(0, 0, 190, 24);
            tfGateway.setColumns(10);
            addGatewayPanel.add(tfGateway);


            // Checkbox
            chkUpFlag = new JCheckBox("UP");
            chkUpFlag.setBounds(110, 100, 44, 24);
            add(chkUpFlag);

            chkGatewayFlag = new JCheckBox("Gateway");
            chkGatewayFlag.setBounds(150, 100, 74, 24);
            add(chkGatewayFlag);

            chkHostFlag = new JCheckBox("Host");
            chkHostFlag.setBounds(220, 100, 74, 24);
            add(chkHostFlag);


            // ComboBox
            addInterfaceComboBox = new JComboBox<>();
            addInterfaceComboBox.setBounds(110, 130, 190, 24);
            add(addInterfaceComboBox);
            updateInterface();
        }

        public void updateInterface() {
            addInterfaceComboBox.removeAllItems();
            for (int i = 0; i < m_NILayer.m_pAdapterList.size(); i++) {
                addInterfaceComboBox.addItem(m_NILayer.m_pAdapterList.get(i).getDescription());
            }
        }

        class setPopupListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btnOK) { // Pushed "OK" button

                }

                if (e.getSource() == btnCancel) { // Pushed "Cancel" button
                    setVisible(false);
                }
            }
        }
    }

    public boolean receive(byte[] input) {
        String s;
        s = new String(input);
        return true;
    }

    public void updateRoutingTable() {

    }

    public void updateCacheTable() {

    }

    public void updateProxyArpEntry() {

    }

    // IP 충돌시 메세지 뜨게함
    public void IPCrash() {
        String msg = "IP주소 충돌이 발생하였습니다.";
        JOptionPane.showMessageDialog(null, msg);
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
