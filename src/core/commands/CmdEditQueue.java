package core.commands;

import core.Constants;
import core.entities.QueueManager;
import core.entities.Server;
import core.exceptions.BadArgumentsException;
import core.exceptions.DoesNotExistException;
import core.util.Utils;
import net.dv8tion.jda.core.entities.Member;

public class CmdEditQueue extends Command{
	
	public CmdEditQueue(){
		this.helpMsg = Constants.EDITQUEUE_HELP;
		this.description = Constants.EDITQUEUE_DESC;
		this.name = Constants.EDITQUEUE_NAME;
		this.adminRequired = true;
	}
	
	@Override
	public void execCommand(Server server, Member member, String[] args) {
		QueueManager qm = server.getQueueManager();
		try{
			if(args.length == 3){
				try{
					qm.editQueue(Integer.valueOf(args[0]), args[1], Integer.valueOf(args[2]));
				}catch(NumberFormatException ex){
					try{
						qm.editQueue(args[0], args[1], Integer.valueOf(args[2]));
					}catch(NumberFormatException e){
						throw new BadArgumentsException("New max player count must be a valid number");
					}
				}
			}else{
				throw new BadArgumentsException();
			}
			qm.updateTopic();
			this.response = Utils.createMessage(String.format("Queue %s edited", args[0]), qm.getHeader(), true);
			System.out.println(success());
		}catch(BadArgumentsException | DoesNotExistException ex){
			this.response = Utils.createMessage("Error!", ex.getMessage(), false);
		}
	}

}
