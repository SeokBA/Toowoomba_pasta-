package router;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class RouterDlg extends JFrame implements BaseLayer {
    public int nUpperLayerCount = 0;
    public int nUnderLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<>();
    BaseLayer UnderLayer;

    private Tools tools = new Tools();

    private RoutingTable routingTable;
    private ARPCacheTable arpCacheTable;
    private ProxyARPTable proxyArpTable;

    private static LayerManager m_LayerMgr = new LayerManager();

    String[] routingHeader = {"Destination", "NetMask", "Gateway", "Flag", "Interface", "Metric"};
    String[] arpHeader = {"IP Address", "Ethernet Address", "Interface", "Flag"};
    String[] proxyHeader = {"IP Address", "Ethernet Address", "Interface"};
    String[][] routingTableStr = {};
    String[][] arpTableStr = {};
    String[][] proxyTableStr = {};

    Container contentPanel;

    JPanel StaticRoutingPanel;
    JPanel ARPCachePanel;
    JPanel ProxyARPPanel;
    JPanel StaticRoutingDisplayPanel;
    JPanel ARPCacheDisplayPanel;
    JPanel ProxyARPDisplayPanel;
    JPanel HostPanel;

    DefaultTableModel routineModel;
    DefaultTableModel arpModel;
    DefaultTableModel proxyModel;

    JTable routingArea;
    JTable arpArea;
    JTable proxyArea;

    JScrollPane scrollARP;
    JScrollPane scrollProxy;
    JScrollPane scrollRouting;

    JButton btnRoutingAdd;
    JButton btnRoutingDelete;
    JButton btnARPDelete;
    JButton btnProxyAdd;
    JButton btnProxyDelete;
    JButton btnGARPSend;
    JButton btnInterface0Start;
    JButton btnInterface1Start;

    JLabel lbInterface_0IP;
    JLabel lbInterface_1IP;
    JLabel lbInterface_0MAC;
    JLabel lbInterface_1MAC;

    setAddressListener setAddressListener = new setAddressListener();
    setMouseListener setMouseListener = new setMouseListener();

    PopupRoutingAdderDlg popupRoutingAdderDlg;
    PopupProxyAdderDlg popupProxyAdderDlg;

    int selected0 = -1, selected1 = -1;

    public static void main(String[] args) {
        m_LayerMgr.addLayer(new RouterDlg("GUI"));
        Tools.setGUILayer((RouterDlg) m_LayerMgr.getLayer("GUI"));
        ((RouterDlg) m_LayerMgr.getLayer("GUI")).routingTable = Tools.getRoutingTable();
        ((RouterDlg) m_LayerMgr.getLayer("GUI")).arpCacheTable = Tools.getARPCacheTable();
        ((RouterDlg) m_LayerMgr.getLayer("GUI")).proxyArpTable = Tools.getProxyARPTable();
        m_LayerMgr.addLayer(new NILayer("NI_L"));
        m_LayerMgr.addLayer(new EthernetLayer("EtherNet_L"));
        m_LayerMgr.addLayer(new IPLayer("IP_L"));
        m_LayerMgr.addLayer(new ARPLayer("ARP_L"));

        m_LayerMgr.addLayer(new NILayer("NI_R"));
        m_LayerMgr.addLayer(new EthernetLayer("EtherNet_R"));
        m_LayerMgr.addLayer(new IPLayer("IP_R"));
        m_LayerMgr.addLayer(new ARPLayer("ARP_R"));

        m_LayerMgr.connectLayers("GUI ( +NI_L ( *EtherNet_L ( *IP_L ( *GUI ) ) *EtherNet_L ( *ARP_L ( *GUI ) *ARP_L ( *IP_L ) ) ) +NI_R ( *EtherNet_R ( *IP_R ( *GUI ) ) *EtherNet_R ( *ARP_R ( *GUI ) *ARP_R ( *IP_R ) ) ) )");
    }

    public RouterDlg(String pName) {
        pLayerName = pName;

        // main
        setTitle("TestRouting");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250, 250, 980, 550);
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

        HostPanel = new JPanel();
        HostPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Host Button", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        HostPanel.setBounds(20, 415, 920, 70);
        contentPanel.add(HostPanel);

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


        // Default Table Model
        routineModel = new DefaultTableModel(routingTableStr, routingHeader);
        arpModel = new DefaultTableModel(arpTableStr, arpHeader);
        proxyModel = new DefaultTableModel(proxyTableStr, proxyHeader);


        // Table
        routingArea = new JTable(routineModel);
        arpArea = new JTable(arpModel);
        proxyArea = new JTable(proxyModel);


        // Scroll panel
        scrollRouting = new JScrollPane(routingArea);
        scrollRouting.setBounds(0, 0, 480, 300);
        StaticRoutingDisplayPanel.add(scrollRouting);

        scrollARP = new JScrollPane(arpArea);
        scrollARP.setBounds(0, 0, 380, 125);
        ARPCacheDisplayPanel.add(scrollARP);

        scrollProxy = new JScrollPane(proxyArea);
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

        btnInterface0Start = new JButton("Start Interface_0");
        btnInterface0Start.setBounds(160, 50, 120, 25);
        btnInterface0Start.addActionListener(setAddressListener);
        HostPanel.add(btnInterface0Start);

        btnGARPSend = new JButton("Send Gratuitous ARP");
        btnGARPSend.setBounds(360, 50, 200, 25);
        btnGARPSend.addActionListener(setAddressListener);
        HostPanel.add(btnGARPSend);

        btnInterface1Start = new JButton("Start Interface_1");
        btnInterface1Start.setBounds(580, 50, 120, 25);
        btnInterface1Start.addActionListener(setAddressListener);
        HostPanel.add(btnInterface1Start);

        // Label
        lbInterface_0IP = new JLabel("Interface_0 IP      : ");
        lbInterface_0IP.setBounds(20, 10, 260, 25);
        lbInterface_0IP.addMouseListener(setMouseListener);
        contentPanel.add(lbInterface_0IP);

        lbInterface_0MAC = new JLabel("Interface_0 MAC : ");
        lbInterface_0MAC.setBounds(20, 30, 260, 25);
        lbInterface_0MAC.addMouseListener(setMouseListener);
        contentPanel.add(lbInterface_0MAC);

        lbInterface_1IP = new JLabel("Interface_1 IP      : ");
        lbInterface_1IP.setBounds(700, 10, 260, 25);
        lbInterface_1IP.addMouseListener(setMouseListener);
        contentPanel.add(lbInterface_1IP);

        lbInterface_1MAC = new JLabel("Interface_1 MAC : ");
        lbInterface_1MAC.setBounds(700, 30, 260, 25);
        lbInterface_1MAC.addMouseListener(setMouseListener);
        contentPanel.add(lbInterface_1MAC);

        setVisible(true);
    }

    class setAddressListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) { // routing add
            if (e.getSource() == btnRoutingAdd) {
                if (popupRoutingAdderDlg == null)
                    popupRoutingAdderDlg = new PopupRoutingAdderDlg();
                popupRoutingAdderDlg.popup();
            }
            if (e.getSource() == btnRoutingDelete) { // routing delete
                int selectRow = routingArea.getSelectedRow();
                if (selectRow == -1) {
                    JOptionPane.showMessageDialog(null, "Select a table item to delete.");
                } else {
                    routingTable.getTable().remove(selectRow);
                    tools.updateRoutingTable();
                }
            }
            if (e.getSource() == btnARPDelete) { // arp delete
                int selectRow = arpArea.getSelectedRow();
                if (selectRow == -1) {
                    JOptionPane.showMessageDialog(null, "Select a table item to delete.");
                } else {
                    arpCacheTable.getTable().remove(arpArea.getValueAt(selectRow, 0));
                    tools.updateARPTable();
                }
            }
            if (e.getSource() == btnProxyAdd) { // proxy add
                if (popupProxyAdderDlg == null)
                    popupProxyAdderDlg = new PopupProxyAdderDlg();
                popupProxyAdderDlg.popup();
            }
            if (e.getSource() == btnProxyDelete) { // proxy delete
                int selectRow = proxyArea.getSelectedRow();
                if (selectRow == -1) {
                    JOptionPane.showMessageDialog(null, "Select a table item to delete.");
                } else {
                    proxyArpTable.getTable().remove(proxyArea.getValueAt(selectRow, 0));
                    tools.updateProxyTable();
                }
            }
            if (e.getSource() == btnGARPSend) {
                EthernetLayer selectEtherNetLayer;
                ARPLayer selectARPLayer;
                IPLayer selectIPLayer;
                String inputIP;
                String inputInterface = JOptionPane.showInputDialog("Select Interface (input 0 or 1)");
                if (inputInterface.equals("0")) {
                    selectEtherNetLayer = (EthernetLayer) m_LayerMgr.getLayer("EtherNet_L");
                    selectARPLayer = (ARPLayer) m_LayerMgr.getLayer("ARP_L");
                    selectIPLayer = (IPLayer) m_LayerMgr.getLayer("IP_L");
                    inputIP = lbInterface_0IP.getText().replace("Interface_0 IP      : ", "");
                } else if (inputInterface.equals("1")) {
                    selectEtherNetLayer = (EthernetLayer) m_LayerMgr.getLayer("EtherNet_R");
                    selectARPLayer = (ARPLayer) m_LayerMgr.getLayer("ARP_R");
                    selectIPLayer = (IPLayer) m_LayerMgr.getLayer("IP_R");
                    inputIP = lbInterface_1IP.getText().replace("Interface_1 IP      : ", "");
                } else {
                    JOptionPane.showMessageDialog(null, "input 0 or 1");
                    return;
                }

                if (inputIP.equals("")) {
                    JOptionPane.showMessageDialog(null, "You did not setting Interface_0 or 1");
                    return;
                }

                String inputHW = JOptionPane.showInputDialog("Input Interface " + inputInterface + " HW Address");
                if (inputHW.equals("")) {
                    JOptionPane.showMessageDialog(null, "You did not enter HW address.");
                    return;
                }

                byte[] data = inputHW.getBytes();
                byte[] hwAddr = tools.stringHWaddrToByte(inputHW);
                byte[] ipAddr = tools.stringIPaddrToByte(inputIP);
                selectEtherNetLayer.setEnetSrcAddress(hwAddr);
                selectARPLayer.setSrcHWAddress(hwAddr);
                selectARPLayer.setSrcPTAddress(ipAddr);
                selectIPLayer.setDstAddress(ipAddr);
                selectIPLayer.send(data, data.length);
            }

            if (e.getSource() == btnInterface0Start) {
                if (selected0 == -1)
                    return;
                ((NILayer) m_LayerMgr.getLayer("NI_L")).setAdapterNumber(selected0);
                btnInterface0Start.setEnabled(false);
            }

            if (e.getSource() == btnInterface1Start) {
                if (selected1 == -1)
                    return;
                ((NILayer) m_LayerMgr.getLayer("NI_R")).setAdapterNumber(selected1);
                btnInterface1Start.setEnabled(false);
            }
        }
    }

    class setMouseListener implements MouseListener {
        PopupSelectNICDlg popupSelectNICDlg = new PopupSelectNICDlg(new NILayer("NI"));

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() == lbInterface_0IP || e.getSource() == lbInterface_0MAC) {
                popupSelectNICDlg.popup("Interface_0 NIC", false);
            }

            if (e.getSource() == lbInterface_1IP || e.getSource() == lbInterface_1MAC) {
                popupSelectNICDlg.popup("Interface_1 NIC", true);
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
                        selected0 = selected;
                        //((NILayer) m_LayerMgr.getLayer("NI_L")).setAdapterNumber(selected);
                        ((EthernetLayer) m_LayerMgr.getLayer("EtherNet_L")).setEnetSrcAddress(tools.hwAddrStringToByte(macAddressStr));
                        ((ARPLayer) m_LayerMgr.getLayer("ARP_L")).setSrcHWAddress(tools.hwAddrStringToByte(macAddressStr));
                        ((ARPLayer) m_LayerMgr.getLayer("ARP_L")).setSrcPTAddress(tools.ipAddrStringToByte(ipAddressStr));
                        ((IPLayer) m_LayerMgr.getLayer("IP_L")).setSrcAddress(tools.ipAddrStringToByte(ipAddressStr));

                        lbInterface_0IP.setText("Interface_0 IP      : " + ipAddressStr);
                        lbInterface_0MAC.setText("Interface_0 MAC : " + macAddressStr);
                    } else {
                        selected1 = selected;
                        //((NILayer) m_LayerMgr.getLayer("NI_R")).setAdapterNumber(selected);
                        ((EthernetLayer) m_LayerMgr.getLayer("EtherNet_R")).setEnetSrcAddress(tools.hwAddrStringToByte(macAddressStr));
                        ((ARPLayer) m_LayerMgr.getLayer("ARP_R")).setSrcHWAddress(tools.hwAddrStringToByte(macAddressStr));
                        ((ARPLayer) m_LayerMgr.getLayer("ARP_R")).setSrcPTAddress(tools.ipAddrStringToByte(ipAddressStr));
                        ((IPLayer) m_LayerMgr.getLayer("IP_R")).setSrcAddress(tools.ipAddrStringToByte(ipAddressStr));

                        lbInterface_1IP.setText("Interface_1 IP      : " + ipAddressStr);
                        lbInterface_1MAC.setText("Interface_2 MAC : " + macAddressStr);
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

        public PopupRoutingAdderDlg() {
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
            addInterfaceComboBox.addItem("0");
            addInterfaceComboBox.addItem("1");
            add(addInterfaceComboBox);
        }

        public void popup() {
            tfDestination.setText("");
            tfNetmask.setText("");
            tfGateway.setText("");
            chkUpFlag.setSelected(false);
            chkGatewayFlag.setSelected(false);
            chkHostFlag.setSelected(false);
            addInterfaceComboBox.setSelectedIndex(0);
            setVisible(true);
        }

        class setPopupListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btnOK) { // Pushed "OK" button
                    String dstIPAddr = tfDestination.getText();
                    String netMask = tfNetmask.getText();
                    String gateWay = tfGateway.getText();
                    String flag = "";
                    if (chkUpFlag.isSelected())
                        flag += "U";
                    if (chkGatewayFlag.isSelected())
                        flag += "G";
                    if (chkHostFlag.isSelected())
                        flag += "H";
                    int interface_num = addInterfaceComboBox.getSelectedIndex();
                    int metric = 1;
                    routingTable.getTable().add(new RoutingRecord(dstIPAddr, netMask, gateWay, flag, interface_num, metric));
                    tools.updateRoutingTable();
                    setVisible(false);
                }

                if (e.getSource() == btnCancel) { // Pushed "Cancel" button
                    setVisible(false);
                }
            }
        }
    }

    public class PopupProxyAdderDlg extends JFrame {
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
            setTitle("Add Proxy ARP Entry");
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            setSize(320, 220);
            setLayout(null);


            // Panel
            addDevicePanel = new JPanel();
            addDevicePanel.setLayout(null);
            addDevicePanel.setBounds(130, 23, 150, 20);
            add(addDevicePanel);

            addProxyIPPanel = new JPanel();
            addProxyIPPanel.setLayout(null);
            addProxyIPPanel.setBounds(130, 63, 150, 20);
            add(addProxyIPPanel);

            addProxyEtherPanel = new JPanel();
            addProxyEtherPanel.setLayout(null);
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

            lbAddProxyIP = new JLabel("IP Address");
            lbAddProxyIP.setBounds(20, 60, 130, 24);
            add(lbAddProxyIP);

            lbAddProxyEther = new JLabel("Ethernet Address");
            lbAddProxyEther.setBounds(20, 100, 130, 24);
            add(lbAddProxyEther);


            // ComboBox
            String[] hostArray = {"0", "1"};
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

        public void popup() {
            tfAddProxyIP.setText("");
            tfAddProxyEther.setText("");
            diviceSelectBox.setSelectedIndex(0);
            setVisible(true);
        }

        class setPopupListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btnOK) { // Pushed "OK" button
                    String ipAddr = tfAddProxyIP.getText();
                    String hwAddr = tfAddProxyEther.getText();
                    int interfaceNum = diviceSelectBox.getSelectedIndex();
                    proxyArpTable.getTable().put(tfAddProxyIP.getText(), new ProxyARPRecord(ipAddr, hwAddr, interfaceNum));
                    tools.updateProxyTable();
                    setVisible(false);
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
