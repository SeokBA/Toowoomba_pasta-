//import org.jnetpcap.PcapAddr;
//import org.jnetpcap.PcapIf;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class RouterDlg extends JFrame implements BaseLayer {
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    BaseLayer UnderLayer; //

    private static LayerManager m_LayerMgr = new LayerManager();

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

    DefaultListModel RoutingModel;
    DefaultListModel ARPModel;
    DefaultListModel proxyModel;

    JList staticRoutingArea;
    JList ARPArea;
    JList proxyArea;

    PopupRoutingAdderDlg popupRoutingAdderDlg;

    public static void main(String[] args) {
        //m_LayerMgr.AddLayer(new NILayer("NI"));
        //m_LayerMgr.AddLayer(new EthernetLayer("EtherNet"));
        //m_LayerMgr.AddLayer(new IPLayer("IP"));
        //m_LayerMgr.AddLayer(new TCPLayer("TCP"));
        //m_LayerMgr.AddLayer(new ChatAppLayer("Chat"));
        //m_LayerMgr.AddLayer(new ARPLayer("ARP"));
        //m_LayerMgr.ConnectLayers(" NI ( *EtherNet ( +IP ( *TCP ( *Chat ( *GUI ) ) -ARP( *EtherNet ) ) ) ) ");
        m_LayerMgr.AddLayer(new RouterDlg("GUI")); // gui test
    }

    public RouterDlg(String pName) {
        pLayerName = pName;

        // main
        setTitle("TestRouting");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250, 250, 780, 440);
        contentPanel = new JPanel();
        ((JComponent) contentPanel).setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPanel);
        contentPanel.setLayout(null);


        // Panel
        StaticRoutingPanel = new JPanel();
        StaticRoutingPanel.setLayout(null);
        StaticRoutingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Static Routing Table", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        StaticRoutingPanel.setBounds(20, 40, 360, 345);
        contentPanel.add(StaticRoutingPanel);

        ARPCachePanel = new JPanel();
        ARPCachePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ARP Cache Table", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        ARPCachePanel.setBounds(400, 40, 360, 170);
        contentPanel.add(ARPCachePanel);
        ARPCachePanel.setLayout(null);

        ProxyARPPanel = new JPanel();
        ProxyARPPanel.setLayout(null);
        ProxyARPPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Proxy ARP Table", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        ProxyARPPanel.setBounds(400, 215, 360, 170);
        contentPanel.add(ProxyARPPanel);

        StaticRoutingDisplayPanel = new JPanel();
        StaticRoutingDisplayPanel.setLayout(null);
        StaticRoutingDisplayPanel.setBounds(10, 15, 340, 300);
        StaticRoutingPanel.add(StaticRoutingDisplayPanel);

        ARPCacheDisplayPanel = new JPanel();
        ARPCacheDisplayPanel.setBounds(10, 15, 340, 125);
        ARPCachePanel.add(ARPCacheDisplayPanel);
        ARPCacheDisplayPanel.setLayout(null);

        ProxyARPDisplayPanel = new JPanel();
        ProxyARPDisplayPanel.setBounds(10, 15, 340, 125);
        ProxyARPPanel.add(ProxyARPDisplayPanel);
        ProxyARPDisplayPanel.setLayout(null);


        // Scroll panel
        scrollRouting = new JScrollPane();
        scrollRouting.setBounds(0, 0, 340, 300);
        StaticRoutingDisplayPanel.add(scrollRouting);

        scrollARP = new JScrollPane();
        scrollARP.setBounds(0, 0, 340, 125);
        ARPCacheDisplayPanel.add(scrollARP);

        scrollProxy = new JScrollPane();
        scrollProxy.setBounds(0, 0, 340, 125);
        ProxyARPDisplayPanel.add(scrollProxy);


        // Button
        btnRoutingAdd = new JButton("Add");
        btnRoutingAdd.setBounds(34, 315, 120, 25);
        btnRoutingAdd.addActionListener(new setAddressListener());
        StaticRoutingPanel.add(btnRoutingAdd);

        btnRoutingDelete = new JButton("Delete");
        btnRoutingDelete.setBounds(198, 315, 120, 25);
        btnRoutingDelete.addActionListener(new setAddressListener());
        StaticRoutingPanel.add(btnRoutingDelete);

        btnARPDelete = new JButton("Delete");
        btnARPDelete.setBounds(120, 140, 120, 25);
        btnARPDelete.addActionListener(new setAddressListener());
        ARPCachePanel.add(btnARPDelete);

        btnProxyAdd = new JButton("Add");
        btnProxyAdd.setBounds(34, 140, 120, 25);
        btnProxyAdd.addActionListener(new setAddressListener());
        ProxyARPPanel.add(btnProxyAdd);

        btnProxyDelete = new JButton("Delete");
        btnProxyDelete.setBounds(198, 140, 120, 25);
        btnProxyDelete.addActionListener(new setAddressListener());
        ProxyARPPanel.add(btnProxyDelete);


        // List Model
        RoutingModel = new DefaultListModel();
        ARPModel = new DefaultListModel();
        proxyModel = new DefaultListModel();


        // List area
        staticRoutingArea = new JList(RoutingModel);
        scrollRouting.setViewportView(staticRoutingArea);

        ARPArea = new JList(ARPModel);
        scrollARP.setViewportView(ARPArea);

        proxyArea = new JList(proxyModel);
        scrollProxy.setViewportView(proxyArea);


        setVisible(true);
    }
    public class PopupRoutingAdderDlg extends JFrame{
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

        public PopupRoutingAdderDlg(NILayer m_NILayer) {
            this.m_NILayer = m_NILayer;

            // main
            setTitle("Routing Table Entry");
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            setSize(320,220);
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
            btnOK.addActionListener(new setPopupListener());
            add(btnOK);

            btnCancel = new JButton("Cancel");
            btnCancel.setBounds(160, 165, 100, 27);
            btnCancel.addActionListener(new setPopupListener());
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
            chkUpFlag.setBounds(110, 100, 50, 24);
            add(chkUpFlag);

            chkGatewayFlag = new JCheckBox("Gateway");
            chkGatewayFlag.setBounds(150, 100, 200, 24);
            add(chkGatewayFlag);

            chkHostFlag = new JCheckBox("Host");
            chkHostFlag.setBounds(230, 100, 100, 24);
            add(chkHostFlag);


            // ComboBox
            addInterfaceComboBox = new JComboBox<>();
            addInterfaceComboBox.setBounds(110, 130, 190, 24);
            addInterfaceComboBox.addActionListener(new setPopupListener());
            add(addInterfaceComboBox);
            // updateInterface();
        }

        public void updateInterface() {
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

    class setAddressListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnRoutingAdd) {
                if (popupRoutingAdderDlg == null)
                    popupRoutingAdderDlg = new PopupRoutingAdderDlg((NILayer)m_LayerMgr.GetLayer("NI"));
                // popupRoutingAdderDlg.updateInterface();
                popupRoutingAdderDlg.setVisible(true);
                RoutingModel.addElement("routing add");
            }
            if (e.getSource() == btnRoutingDelete) {
                RoutingModel.addElement("routing delete");
            }
            if (e.getSource() == btnARPDelete) {
                RoutingModel.addElement("arp delete");
            }
            if (e.getSource() == btnProxyAdd) {
                RoutingModel.addElement("proxy add");
            }
            if (e.getSource() == btnProxyDelete) {
                RoutingModel.addElement("proxy delete");
            }
        }
    }
    public boolean Receive(byte[] input) {
        String s;
        s = new String(input);
        return true;
    }

    @Override
    public void SetUnderLayer(BaseLayer pUnderLayer) {
        // TODO Auto-generated method stub
        if (pUnderLayer == null)
            return;
        this.p_UnderLayer = pUnderLayer;
    }

    @Override
    public void SetUpperLayer(BaseLayer pUpperLayer) {
        // TODO Auto-generated method stub
        if (pUpperLayer == null)
            return;
        this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
        // nUpperLayerCount++;
    }

    @Override
    public String GetLayerName() {
        // TODO Auto-generated method stub
        return pLayerName;
    }

    @Override
    public BaseLayer GetUnderLayer() {
        // TODO Auto-generated method stub
        if (p_UnderLayer == null)
            return null;
        return p_UnderLayer;
    }

    @Override
    public BaseLayer GetUpperLayer(int nindex) {
        // TODO Auto-generated method stub
        if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
            return null;
        return p_aUpperLayer.get(nindex);
    }

    @Override
    public void SetUpperUnderLayer(BaseLayer pUULayer) {
        this.SetUpperLayer(pUULayer);
        pUULayer.SetUnderLayer(this);
    }
}
