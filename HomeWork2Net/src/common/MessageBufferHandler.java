package common;

public class MessageBufferHandler {

    public String makeMesString(Message mes){

        return String.valueOf(mes.getWord()) + "#" + String.valueOf(mes.getTries()) + "#" + String.valueOf(mes.getScore()) + "#" + mes.getStatus();

    }

    public Message makeMesMessage(String bufferMes){
        String[] parts = bufferMes.split("#");
        Status type = Status.valueOf(parts[3].toUpperCase());
        return new Message(parts[0].toCharArray(), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), type);

    }

}
