package agent;

import java.io.*;
import java.util.*;
import java.net.*;

public class AgentMain{
	public static void main(String[] args)throws Exception{
		//Titkosügynökök darabszám, várakozás hossza
		int n = Integer.parseInt(args[0]);
		int m = Integer.parseInt(args[1]);
		int t1 = Integer.parseInt(args[2]);
		int t2 = Integer.parseInt(args[3]);
		System.out.println("Az ugynokok szama: " + n + ", " + m + ", A varakozas: " + t1 + ", " + t2);
		
		//Threadeket ebben tároljuk 
		ArrayList<Agent> threads = new ArrayList<>();
		for(int i = 0; i < n+m; i++){
			if(i<n){
				threads.add(new Agent(1, n, m, t1, t2));
				threads.get(i).start();
			}else{
				threads.add(new Agent(2, m, n, t1, t2));
				threads.get(i).start();
			}
			if(i == n-1){
				Agent.setCount(1);
			}
		}
		
		for(Agent t: threads){
			t.join();
		}
	}
}

class Agent extends Thread{
	
	int serverPORT;
	int clientPORT;
	int agency;
	int agentNumber;
	int otherAgencySize;
	static boolean gameOver;
	static int teamA;
	static int teamB;
	static int count = 1;
	String fileName;
	boolean living;
	int lower;
	int upper;
	String alias;
	ArrayList<String> aliases;
	HashMap<String, Integer> knownAlias;
	HashMap<String, Integer> knownAgentNumbers;
	HashMap<String, ArrayList<Integer>> wrongGuesses;
	ArrayList<String> knownSecrets;
	ArrayList<String> toldSecrets;
	ArrayList<String> enemySecrets;
	
	public Agent(int agency, int teamA, int teamB, int lower, int upper){
		Random rand = new Random();
		this.serverPORT = rand.nextInt(101) + 20000;
		do{
			this.clientPORT = rand.nextInt(101) + 20000;
		}while(clientPORT == serverPORT);
		this.agency = agency;
		this.agentNumber = count;
		count++;
		this.living = true;
		this.teamA = teamA;
		this.teamB = teamB;
		this.gameOver = true;
		this.otherAgencySize = teamB;
		this.fileName = "agent" + Integer.toString(this.agency) + "-" + Integer.toString(this.agentNumber) + ".txt";
		System.out.println(this.fileName);
		this.lower = lower;
		this.upper = upper;
		try{
			Scanner sc = new Scanner(new File(this.fileName));
			this.alias = sc.nextLine();
			aliases = new ArrayList<String>(Arrays.asList(alias.split("\\s+")));
			knownSecrets = new ArrayList<>();
			knownSecrets.add(sc.nextLine());
		}catch(Exception e){
			System.out.println("Nincs elso sor");
		}
		knownAlias = new HashMap<>();
		toldSecrets = new ArrayList<>();
		knownAgentNumbers = new HashMap<>();
		wrongGuesses = new HashMap<String, ArrayList<Integer>>();
		enemySecrets = new ArrayList<>();
		
	}
	
	@Override
	public void run(){
		
		AgentServer server = new AgentServer();
		AgentClient client = new AgentClient();
		
		server.start();
		client.start();
		
		try{
			server.join();
			client.join();
		}catch(Exception e){
			System.out.println("Valami baj van a joinnal");
		}
	}
	
	public static void setCount(int c){
		count = c;
	}
	
	public int getAgentNumber(){
		return agentNumber;
	}
	
	public int getAgency(){
		return agency;
	}
	
	public int getServerPort(){
		return this.serverPORT;
	}
	
	public int getClientPort(){
		return this.clientPORT;
	}
	
	public void addSecret(ArrayList<String> a, String s){
		if(!a.contains(s)){
			a.add(s);
		}
	}
	
	public int generateRandomNumber(int up, int low){
		Random rand = new Random();
		int n = rand.nextInt(up-low) + low;
		return n;
	}
	
	public String sendRandomSecret(PrintWriter pw, ArrayList<String> secrets){
		Random rand = new Random();
		int n = rand.nextInt(secrets.size());
		
		pw.println(secrets.get(n));
		pw.flush();
		return secrets.get(n);
	}
	
	class AgentServer extends Thread{
		@Override
		public void run(){
			startServer(upper);
		}
		
		public void startServer(int upperBoundary){
			while(living && gameOver){
				try(
					ServerSocket ss = new ServerSocket(serverPORT);

				){
					System.out.println("SZERVER " + getAgency() + "-" + getAgentNumber() + ", PORT: " + serverPORT + " ENNYIT VAROK: " + upperBoundary);
					
					ss.setSoTimeout(upperBoundary);
					Socket s = ss.accept();
					Scanner sc = new Scanner(s.getInputStream());
					PrintWriter pw = new PrintWriter(s.getOutputStream());
					
					//1. elküld 1 random nevet
					sendRandomAlias(pw, aliases);
					
					//3.Megkapja a tippet (vagy a helyes választ)
					String guess = sc.nextLine();
					
					if(getAgency() == Integer.parseInt(guess)){
						//elküldi hogy "OK"
						pw.println("OK");
						pw.flush();
						//Visszakap egy választ (vagy "OK" vagy "???")
						String answer = sc.nextLine();
						//Ha "OK"
						if(answer.equals("OK")){
							 //mindketten elküldenek egy-egy titkos szöveget a másiknak, amit ismernek, és ezután bontják a kapcsolatot.
							String toldSecret = sendRandomSecret(pw, knownSecrets);
							//megkapja amit a kliens küld
							String aSecret = sc.nextLine();
							System.out.println("SZERVER " + getAgency() + "-" + getAgentNumber() + "port:  " + getServerPort() + " A TITOK AMIT KAPTAM UGYNOKTARSAMTOL: " + aSecret);
							//elteszi amit a kliens küld
							addSecret(knownSecrets, aSecret);
						}else if(answer.equals("???")){
							String numb = sc.nextLine();
							System.out.println("A sorszam amit kaptam: " + numb);
							//A szerver azonnal bontja a kapcsolatot, ha téves a sorszám.
							if(agentNumber != Integer.parseInt(numb)){
								System.out.println("SZERVER " + getAgency() + "-" + getAgentNumber() + " KAPCSOLAT BONTVA");
								s.close();
							}else{
								//Ha helyes a sorszám, elküldi az általa ismert titkok egyikét.
								String toldSecret = knownSecrets.get(generateRandomNumber(knownSecrets.size(), 0));
								//Ha már elárulta a ezt a titkot akkor másik titkot keres
								while(toldSecrets.contains(toldSecret)){
									toldSecret = knownSecrets.get(generateRandomNumber(knownSecrets.size(), 0));
								}
								pw.println(toldSecret);
								pw.flush();
								toldSecrets.add(toldSecret);
								if(toldSecrets.size()==knownSecrets.size()){
									living=false;
									System.out.println("-------MEGHALTAM------" + getAgency() + "-" + getAgentNumber());
									if(getAgency()== 1){
										teamA--;
									}else if(getAgency()== 2){
										teamB--;
									}
								}
							}
						}
						
						if(teamA == 0){
							gameOver=false;
							System.out.println("NYERT: 2 AGENCY");
						}else if(teamB == 0){
							gameOver=false;
							System.out.println("NYERT: 1 AGENCY");	
						}
						
						//Ezután, ha már minden általa ismert titkot elárult, 
						//a szerveroldali ügynök le van tartóztatva,
					}else{
						//Különben bontja a kapcsolatot
						s.close();
					}
					s.close();
			
				}catch(Exception e){
					//System.err.println("Caught IOException: " + e.getMessage());
				}finally{
					serverPORT = generateRandomNumber(20101, 20000);
				}
			}
		}
		
		public void sendRandomAlias(PrintWriter pw, ArrayList<String> aliases){
			Random rand = new Random();
			int n = rand.nextInt(aliases.size());
		
			pw.println(aliases.get(n));
			pw.flush();
		}
	}
	
	class AgentClient extends Thread{
		@Override
		public void run(){
			startClient(lower, upper);
		}
		
		public void startClient(int lowerBoundary, int upperBoundary){
			while(living && gameOver){
				try(
					Socket s = new Socket("localhost", clientPORT);
					PrintWriter pw = new PrintWriter(s.getOutputStream());
					Scanner sc = new Scanner(s.getInputStream());
				){					
					System.out.println("KLIENS " + getAgency() + "-" + getAgentNumber() + "CSATLAKOZTAM EGY SZERVERHEZ");
					//1. magkap random nevet
					String randomName = sc.nextLine();

					//Erre a kliens elküldi azt, hogy szerinte a szerver melyik ügynökséghez tartozik.
					//Ha tudja nem tippel:
			
					Random rand = new Random();
					int n = rand.nextInt(2) + 1;
	
					if(knownAlias.containsKey(randomName)){
						n = knownAlias.get(randomName);
						pw.println(n);
						pw.flush();
						//Ha nem tudja tippel
					}else{
						pw.println(n);
						pw.flush();
					}
					//Ha kap most választ annak "OK" -nak kell lenni, mert különben bontva lett a kapcsolat
					String answer = sc.nextLine();
					if(!knownAlias.containsKey(randomName)){
						knownAlias.put(randomName, n);
					}
					
					//5. Ha azonos ügynökséghez tartozik, elküldi az OK szöveget, 
						
					if(n == agency){
						pw.println("OK");
						pw.flush();
						//majd mindketten elküldenek egy-egy titkos szöveget a másiknak, amit ismernek, és ezután bontják a kapcsolatot.
						sendRandomSecret(pw, knownSecrets);
						//megkapja amit a kliens küld
						String aSecret = sc.nextLine();
						System.out.println("KLIENS: " + getAgency() + "-" + getAgentNumber() + " KAPTAM EGY TITKOT UGYNOKTARSAMTOL: " + aSecret);
						//elteszi amit a kliens küld
						addSecret(knownSecrets, aSecret);
					}else{
						//A kliens, ha a másik ügynökséghez tartozik elküldi a ??? szöveget, majd egy számot, ami szerinte a másik ügynök sorszáma lehet. 
						//(Ha már találkozott vele, akkor olyan tippet nem ad, ami biztosan téves.)
						pw.println("???");
						pw.flush();
						//A névhez készítek egy listát amihez hozzáadom a rossz tippet, ha jó lesz kitörlöm belőle
						ArrayList<Integer> wGuess = new ArrayList<>();
						wrongGuesses.put(randomName, wGuess);
						int guess;
						do{
							guess = generateRandomNumber(otherAgencySize+1,1);
						}while(wGuess.contains(guess));
						wGuess.add(guess);
						wrongGuesses.put(randomName, wGuess);
						
						pw.println(guess);
						pw.flush();
						//Ha helyes volt a válasz kap egy titkot
						answer = sc.nextLine();
						System.out.println("KLIENS: " + getAgency() + "-" + getAgentNumber() + " JOL TIPPELTEM, KAPTAM EGY TITKOT: " + answer);
						//Kitörlöm, mivel jó lett a tipp
						wGuess.remove(Integer.valueOf(guess));
						wrongGuesses.put(randomName, wGuess);
						addSecret(enemySecrets, answer);
					}
					if(enemySecrets.size() == otherAgencySize){
						gameOver = false;
						System.out.println("NYERT: " + getAgency());
					}
					
				}catch(Exception e){
					//System.err.println("Caught IOException: " + e.getMessage());
				}finally{
					
					
					
					try{
						int newRand = generateRandomNumber(upperBoundary, lowerBoundary); 
						System.out.println("KLIENS: " + getAgency() + "-" + getAgentNumber() +" PORT: " + getClientPort() + " ALSZOM:: " + newRand);
						Thread.sleep(newRand);
					}catch(Exception ex){
						System.err.println("Caught IOException: " + ex.getMessage());
					}
				do{
					clientPORT = generateRandomNumber(20101,20000);
				}while(clientPORT==getServerPort());
				}
			}
		}
	}
}
