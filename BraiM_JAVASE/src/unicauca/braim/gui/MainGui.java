package unicauca.braim.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.SliderUI;

import unicauca.braim.emotiv.Edk;
import unicauca.braim.emotiv.EdkErrorCode;
import unicauca.braim.emotiv.EmoState;
import unicauca.braim.json.MiJsonObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import javax.swing.JTextPane;
import javax.swing.JTextField;

public class MainGui extends JFrame {

	
	
	
	
	private JPanel contentPane;
	private static JSlider slider_A;
	private static JSlider slider_E;
	private static JSlider slider_M;
	private static JSlider slider_F;
	private static JTextField nodo_0;
	private static JTextField nodo_1;
	private static JTextField nodo_2;
	private static JTextField nodo_3;
	private static JTextField nodo_4;
	private static JTextField nodo_5;
	private static JTextField nodo_6;
	private static JTextField nodo_7;
	private static JTextField nodo_8;
	private static JTextField nodo_9;
	private static JTextField nodo_10;
	private static JTextField nodo_11;
	private static JTextField nodo_12;
	private static JTextField nodo_13;
	private  static MiJsonObject obj;
	private static Gson gson;
	private static int cantidadJason;
	static int contador=0;
	private static float timestamp_date;

	private static String output;
	private static MongoClient mongoClient;
	private static DB db;
	private static DBCollection table;
	private static BasicDBObject document;

	
	//cambiooooooooooooooooooooooooooookjhgk
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		definirMongoDb();
		
		 document = new BasicDBObject();

		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainGui frame = new MainGui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
		 cantidadJason = 1;
		 obj= new MiJsonObject();
		 gson = new Gson();
		
		Pointer eEvent = Edk.INSTANCE.EE_EmoEngineEventCreate();
		Pointer eState = Edk.INSTANCE.EE_EmoStateCreate();
		
		IntByReference userID = null;
		IntByReference nSamplesTaken = null;
	//	short composerPort = 1726;
		short composerPort = 3008;
		int option = 1;
		int state = 0;
		
		float secs = 1;
		boolean readytocollect = false;
		
		userID = new IntByReference();
		nSamplesTaken = new IntByReference();
		
		switch(option){
		case 1:{
			if (Edk.INSTANCE.EE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
				System.out.println("Emotiv Engine start up failed.");
				return;
			}
			break;
		}
		case 2:
		{
			System.out.println("Target IP of EmoComposer: [127.0.0.1] ");

			if (Edk.INSTANCE.EE_EngineRemoteConnect("127.0.0.1", composerPort, "Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
				
				System.out.println("Cannot connect to EmoComposer on [127.0.0.1]");
				return;
			}
			System.out.println("Connected to EmoComposer on [127.0.0.1]");
			break;
		}
		default:
			System.out.println("Invalid option...");
			return;
		
		
		}
		
		Pointer hData = Edk.INSTANCE.EE_DataCreate();
		Edk.INSTANCE.EE_DataSetBufferSizeInSec(secs);
	//	System.out.print("Buffer size in secs: ");
	//	System.out.println(secs);
		
		while (true){
			state = Edk.INSTANCE.EE_EngineGetNextEvent(eEvent);
			if (state == EdkErrorCode.EDK_OK.ToInt()){
				int eventType = Edk.INSTANCE.EE_EmoEngineEventGetType(eEvent);
				Edk.INSTANCE.EE_EmoEngineEventGetUserId(eEvent, userID);
				
				//log emocional
				
				if (eventType == Edk.EE_Event_t.EE_EmoStateUpdated.ToInt()){
					Edk.INSTANCE.EE_EmoEngineEventGetEmoState(eEvent, eState);
					
					float timestamp = EmoState.INSTANCE.ES_GetTimeFromStart(eState); // Timestamp para el evento que llega
					timestamp_date = System.currentTimeMillis();
					
				//	System.out.print("ExcitementShortTerm: ");
				//	System.out.println(EmoState.INSTANCE.ES_AffectivGetExcitementShortTermScore(eState));
					
					float e1 = EmoState.INSTANCE.ES_AffectivGetExcitementShortTermScore(eState);
					float e2 = EmoState.INSTANCE.ES_AffectivGetEngagementBoredomScore(eState);
					float e3 = EmoState.INSTANCE.ES_AffectivGetMeditationScore(eState);
					float e4 = EmoState.INSTANCE.ES_AffectivGetFrustrationScore(eState);
					float e5 = EmoState.INSTANCE.ES_AffectivGetExcitementLongTermScore(eState);
					
					ponerAffectiveValues(e1,e2,e3,e4,e5);
					contador++;
					llenarJson_Affective(e1,e2,e3,e4,e5,contador,timestamp,timestamp_date);
					generarJson();
					
					document.put("Exci", e1);
					document.put("Enga", e2);
					document.put("Medi", e3);
					document.put("Frus", e4);
					document.put("Exci_lon", e5);
					
															
					
				}
				
				
				//Log para los nodos
				
					if (eventType == Edk.EE_Event_t.EE_UserAdded.ToInt())
					if(userID != null){
						System.out.println("User added");
						Edk.INSTANCE.EE_DataAcquisitionEnable(userID.getValue(),true);
						readytocollect = true;
					}
				
				
				}
			else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
				System.out.println("Internal error in Emotiv Engine!");
				break;
			}
			
			if (readytocollect){
				Edk.INSTANCE.EE_DataUpdateHandle(0, hData);
				Edk.INSTANCE.EE_DataGetNumberOfSample(hData, nSamplesTaken);
				
				if (nSamplesTaken != null){
					if (nSamplesTaken.getValue()!=0){
						//System.out.print("Updated: ");
					//	System.out.println(nSamplesTaken.getValue());
						
						double[] data = new double[nSamplesTaken.getValue()];
						for (int sampleIdx=0 ; sampleIdx<nSamplesTaken.getValue() ; ++ sampleIdx) {
							for (int i = 0 ; i < 14 ; i++) {
								Edk.INSTANCE.EE_DataGet(hData, i, data, nSamplesTaken.getValue());
								//Imprimir data[sampleIdx]
								publicarOutput(data[sampleIdx],i);
								llenarJson_nodos(data[sampleIdx],i);
								
								document.put("nodo "+i+": ", data[sampleIdx]);
								
							}
						}
					}
				}
			}
			
			
		}
		Edk.INSTANCE.EE_EngineDisconnect();
		Edk.INSTANCE.EE_EmoStateFree(eState);
    	Edk.INSTANCE.EE_EmoEngineEventFree(eEvent);
    	
    	System.out.println("Disconnected!");
		
	}

	private static void definirMongoDb() {
		// TODO Auto-generated method stub
		try {
			 mongoClient = new MongoClient("localhost",27017);
			 db = mongoClient.getDB("BraimDB_prueba");
			 
			 table = db.getCollection("EE_info");
			 
				List<String> dbs = mongoClient.getDatabaseNames();
				for(String db : dbs){
					System.out.println(db);
				}
				
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

	private static void llenarJson_nodos(double valor, int nodo) {
		// TODO Auto-generated method stub
switch (nodo){
		
		case 0: obj.setNodo_0(valor);
			break;
		case 1:obj.setNodo_1(valor);
			break;
		case 2: obj.setNodo_2(valor);
			break;
		case 3: obj.setNodo_3(valor);
			break;
		case 4: obj.setNodo_4(valor);
			break;
		case 5: obj.setNodo_5(valor);
			break;
		case 6: obj.setNodo_6(valor);
			break;
		case 7: obj.setNodo_7(valor);
			break;
		case 8: obj.setNodo_8(valor);
			break;
		case 9: obj.setNodo_9(valor);
			break;
		case 10: obj.setNodo_10(valor);
			break;
		case 11: obj.setNodo_11(valor);
			break;
		case 12: obj.setNodo_12(valor);
			break;
		case 13: obj.setNodo_13(valor);
			break;
			
		}
	}

	private static void generarJson() {
		// TODO Auto-generated method stub
		
		String json = gson.toJson(obj);
		 
		 // output = output +"\n"+json;
		 output = output +","+json;
		
	}
	
	public void generar_archivo(){
		try {
			//write converted json data to a file named "file.json"
			FileWriter writer = new FileWriter("/home/jesus/Escritorio/file.json");
			writer.write(output);
			writer.close();
	 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// table.insert(document);
	}

	private static void llenarJson_Affective(float valor_A,float valor_E,float valor_M,float valor_F, float valor_lA, int contador, float timestamp, float timestamp_date) {
		// TODO Auto-generated method stub
		obj.setTimestamp(timestamp);
		obj.setTimestamp_date(timestamp_date);
		obj.setI_excitement(valor_A);
		obj.setL_excitement(valor_lA);
		obj.setEngagement(valor_E);
		obj.setFrustation(valor_F);
		obj.setMeditation(valor_M);
		obj.setContador(contador);
		
		
	}

	private static void publicarOutput(double output, int nodo) {
		// TODO Auto-generated method stub
		//textField.setText("");
		
		switch (nodo){
		
		case 0: nodo_0.setText(output+"");
			break;
		case 1: nodo_1.setText(output+"");
			break;
		case 2: nodo_2.setText(output+"");
			break;
		case 3: nodo_3.setText(output+"");
			break;
		case 4: nodo_4.setText(output+"");
			break;
		case 5: nodo_5.setText(output+"");
			break;
		case 6: nodo_6.setText(output+"");
			break;
		case 7: nodo_7.setText(output+"");
			break;
		case 8: nodo_8.setText(output+"");
			break;
		case 9: nodo_9.setText(output+"");
			break;
		case 10: nodo_10.setText(output+"");
			break;
		case 11: nodo_11.setText(output+"");
			break;
		case 12: nodo_12.setText(output+"");
			break;
		case 13: nodo_13.setText(output+"");
			break;
			
		}
		
		
		
	}

	private static void ponerAffectiveValues(float valor_A,float valor_E,float valor_M,float valor_F, float valor_lA) {
		// TODO Auto-generated method stub
		slider_A.setMaximum(10);
		slider_A.setMinimum(0);
		
		slider_E.setMaximum(10);
		slider_E.setMinimum(0);
		
		slider_M.setMaximum(10);
		slider_M.setMinimum(0);
		
		slider_F.setMaximum(10);
		slider_F.setMinimum(0);
		

		
		slider_A.setValue((int)(valor_A*10));
		slider_E.setValue((int)(valor_E*10));
		slider_M.setValue((int)(valor_M*10));
		slider_F.setValue((int)(valor_F*10));
	}

	/**
	 * Create the frame.
	 */
	public MainGui() {
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 520);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblBramFeel = new JLabel("BraiM .. Feel the music");
		lblBramFeel.setBounds(12, 12, 171, 15);
		contentPane.add(lblBramFeel);
		
		slider_A = new JSlider();
		slider_A.setToolTipText("affective");
		slider_A.setBounds(12, 84, 200, 16);
		contentPane.add(slider_A);
		
		JLabel lblAffectiveExcitement = new JLabel("Affective Excitement");
		lblAffectiveExcitement.setBounds(23, 61, 155, 15);
		contentPane.add(lblAffectiveExcitement);
		
		JLabel lblEngagementboredom = new JLabel("Engagement/Boredom");
		lblEngagementboredom.setBounds(22, 112, 161, 15);
		contentPane.add(lblEngagementboredom);
		
		slider_E = new JSlider();
		slider_E.setBounds(12, 139, 200, 16);
		contentPane.add(slider_E);
		
		JLabel lblMeditation = new JLabel("Meditation");
		lblMeditation.setBounds(23, 167, 160, 15);
		contentPane.add(lblMeditation);
		
		 slider_M = new JSlider();
		slider_M.setBounds(12, 194, 200, 16);
		contentPane.add(slider_M);
		
		JLabel lblFrustation = new JLabel("Frustation");
		lblFrustation.setBounds(23, 222, 120, 15);
		contentPane.add(lblFrustation);
		
		 slider_F = new JSlider();
		slider_F.setBounds(12, 249, 200, 16);
		contentPane.add(slider_F);
		
		JLabel lblUser = new JLabel("User");
		lblUser.setBounds(326, 72, 70, 15);
		contentPane.add(lblUser);
		
		JLabel lblSong = new JLabel("Song:");
		lblSong.setBounds(300, 222, 70, 15);
		contentPane.add(lblSong);
		
		JButton btnNewButton = new JButton("User 1");
		btnNewButton.setBounds(290, 112, 117, 101);
	
		contentPane.add(btnNewButton);
		
		JLabel lblSong_1 = new JLabel("Song 1");
		lblSong_1.setBounds(355, 222, 70, 15);
		contentPane.add(lblSong_1);
		
		nodo_0 = new JTextField();
		nodo_0.setBounds(77, 340, 114, 20);
		
		contentPane.add(nodo_0);
		nodo_0.setColumns(10);
		
		JLabel lblOutput = new JLabel("Output");
		lblOutput.setBounds(83, 298, 70, 15);
		contentPane.add(lblOutput);
		
		nodo_1 = new JTextField();
		nodo_1.setBounds(77, 360, 114, 20);
		contentPane.add(nodo_1);
		nodo_1.setColumns(10);
		
		nodo_2 = new JTextField();
		nodo_2.setBounds(77, 380, 114, 20);
		contentPane.add(nodo_2);
		nodo_2.setColumns(10);
		
		nodo_3 = new JTextField();
		nodo_3.setBounds(77, 400, 114, 20);
		contentPane.add(nodo_3);
		nodo_3.setColumns(10);
		
		nodo_4 = new JTextField();
		nodo_4.setBounds(77, 420, 114, 20);
		contentPane.add(nodo_4);
		nodo_4.setColumns(10);
		
		nodo_5 = new JTextField();
		nodo_5.setBounds(77, 440, 114, 20);
		contentPane.add(nodo_5);
		nodo_5.setColumns(10);
		
		nodo_6 = new JTextField();
		nodo_6.setBounds(77, 460, 114, 20);
		contentPane.add(nodo_6);
		nodo_6.setColumns(10);
		
		nodo_7 = new JTextField();
		nodo_7.setBounds(200, 340, 114, 20);
		contentPane.add(nodo_7);
		nodo_7.setColumns(10);
		
		nodo_8 = new JTextField();
		nodo_8.setBounds(200, 360, 114, 20);
		contentPane.add(nodo_8);
		nodo_8.setColumns(10);
		
		nodo_9 = new JTextField();
		nodo_9.setBounds(200, 380, 114, 20);
		contentPane.add(nodo_9);
		nodo_9.setColumns(10);
		
		nodo_10 = new JTextField();
		nodo_10.setBounds(200, 400, 114, 20);
		contentPane.add(nodo_10);
		nodo_10.setColumns(10);
		
		nodo_11 = new JTextField();
		nodo_11.setBounds(200, 420, 114, 20);
		contentPane.add(nodo_11);
		nodo_11.setColumns(10);
		
		nodo_12 = new JTextField();
		nodo_12.setBounds(200, 440, 114, 20);
		contentPane.add(nodo_12);
		nodo_12.setColumns(10);
		
		nodo_13 = new JTextField();
		nodo_13.setBounds(200, 460, 114, 20);
		contentPane.add(nodo_13);
		nodo_13.setColumns(10);
		
		JButton btnGenerarJson = new JButton("Generar Json");
		btnGenerarJson.setBounds(270, 293, 155, 25);
		contentPane.add(btnGenerarJson);
		
		btnGenerarJson.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				generar_archivo();
			}
		});
	}
}
