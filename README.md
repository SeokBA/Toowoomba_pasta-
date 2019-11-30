# Toowoomba_pasta-
## - 공통 참고
  - 만약 branch를 파야한다면, 개인 당 최대 1개의 branch에서 작업 권장
  - 팀원들도 확인을 해야하는 것 등이 아닌 개인이 Test를 위해 사용한 주석 등은 제거/코드 정리를 한 뒤 push

## - 2019.11.18
  - 김석호 :  
      * 주소 계산 등의 method를 Tool class에 정리, 차후에 layer에서 공통적으로 사용가능한 method는 Tool class 에 넣는 것을 권장.

## - 2019.11.19
  - 김석호 :  
      * ARPLayer는 정리 중, 나머지는 대부분 정리완료
## - 2019.11.20
  - 김종운 :
      * 파스칼 표기법이랑 섞어있던 메소드와 변수 -> 카멜 표기법으로 통일  
      * 중복되는 코드 정리  
      * 불필요 코드 일부 제거  
      * 패키지명 소문자 변경  
      * 파일 인코딩 UTF-8 통일  
      * 기본 동작 테스트 완료
## - 2019.11.21
  - 김석호 :  
      * interface_0, 1 select label popup 관련 기능들과 Static Routine Table add 기능 구현
## - 2019.11.22
  - 김종운 :  
      * 코드 전체적으로 수정한 사항 merge, update
  - 김석호 : 
      * updateARPTable, updateProxyTable 메소드 구현 (Tools로 이동, 싱글톤), update method들을 사용하기 위한 코드 변경  
      * routerTable의 Delete 구현
  - 최수민 :
      * RouterDlg의 레이어 연결 수정
## - 2019.11.24
  - 김석호 : 
      * btnARPDelete, btnProxyAdd, btnProxyDelete 기능 구현  
      * Gratuitous ARP를 보내는 btnGARPSend 구현  
      * interface setting 오류 수정
      * IPv4를 끊은 후 NI 연결할 수 있는 btnInterface0Start, btnInterface1Start 기능 구현
  - 김종운 :
      * ArpLayer에서 현재 사용하는 GUI에 맞게 ARP Table 업데이트 수정
  - 민윤기 :
      * Tools의 IntToByte2 index 표준에 맞게 수정
## - 2019.11.24
  - 김석호 : 
      * 테이블에 레코드들이 추가가 되지 않는 오류 수정  
      * ARP와 Ping만을 받을 수 있도록 EthernetLayer 업데이트  
      * 양쪽 interface로 Gratuitous ARP를 보낼 수 있도록 btnGARPSend 수정
## - 2019.11.27
  - 최수민 :  
      * Ping 전송을 구현하기 위해 IPLayer, EthernetLayer 수정
  - 김종운 : 
      * yaml을 이용한 program setting 기능 추가  
      
========================= Ping 전송 성공 =========================
## - 2019.11.30
  - 박원로 :
      * Subnet mask & 연산 기능 구현
      * ARPCacheTable mac 주소 search 기능 구현
      * Routing table Flag 및 gateway를 확인하여 라우팅 기능 구현
  - 장석현 :
      * Routing table Sort 기능 구현
  - 박원로, 김종운, 최수민, 장석현 :
      * Routing 기능 오류 수정, PC 4대 Ping 테스트 진행 및 속도 개선
      
===================== PC 4대 Ping 전송 성공 ======================
