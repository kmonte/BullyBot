package core.commands;

import core.Constants;
import core.entities.QueueManager;
import core.entities.Server;
import core.exceptions.BadArgumentsException;
import core.exceptions.DoesNotExistException;
import core.exceptions.InvalidUseException;
import core.util.Utils;
import net.dv8tion.jda.core.entities.Member;

public class CmdSub extends Command{
	
	public CmdSub(){
		this.helpMsg = Constants.SUB_HELP;
		this.description = Constants.SUB_DESC;
		this.name = Constants.SUB_NAME;
	}
	
	@Override
	public void execCommand(Server server, Member member, String[] args) {
		String targName, subName;
		QueueManager qm = server.getQueueManager();
		try{
			if(args.length < 0 && args.length < 3){
				Member target = server.getMember(args[0]);
				Member substitute = null;
				
				if(args.length == 1){
					substitute = member;
				}else{
					substitute = server.getMember(args[1]);
				}
				
				if(target != null){
					if(substitute != null){
						qm.sub(target.getUser(), substitute.getUser());
						targName = target.getEffectiveName();
						subName = substitute.getEffectiveName();
					}else{
						throw new BadArgumentsException("Substitute player does not exist");
					}
				}else{
					throw new BadArgumentsException("Target player does not exist");
				}
			}else{
				throw new BadArgumentsException();
			}
			qm.updateTopic();
			this.response = Utils.createMessage(String.format("%s has been subbed with %s", targName, subName), "", true);
			System.out.println(success());
		}catch(DoesNotExistException | BadArgumentsException | InvalidUseException ex){
			this.response = Utils.createMessage("Error!", ex.getMessage(), false);
		}
		
	}

}
