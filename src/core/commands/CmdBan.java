package core.commands;

import core.Constants;
import core.entities.Server;
import core.exceptions.BadArgumentsException;
import core.exceptions.DoesNotExistException;
import core.exceptions.InvalidUseException;
import core.util.Utils;
import net.dv8tion.jda.core.entities.Member;

public class CmdBan extends Command{

	public CmdBan(){
		this.name = Constants.BAN_NAME;
		this.description = Constants.BAN_DESC;
		this.helpMsg = Constants.BAN_HELP;
		this.adminRequired = true;
		this.pugCommand = false;
	}
	
	@Override
	public void execCommand(Server server, Member member, String[] args) {
		String pName;
		try{
			if(args.length == 1){
				Member m = server.getMember(args[0]);
				if (m != null){
					pName = m.getEffectiveName();
					if(!server.isAdmin(m)){
						server.banUser(m.getUser().getId());
					}else{
						throw new InvalidUseException("Cannot ban an admin");
					}
				}else{
					throw new DoesNotExistException("User");
				}
			}else{
				throw new BadArgumentsException();
			}
			this.response = Utils.createMessage(String.format("`%s banned`", pName));
			System.out.println(success());
		}catch(BadArgumentsException | DoesNotExistException | InvalidUseException ex){
			this.response = Utils.createMessage("Error!", ex.getMessage(), false);
		}
	}
}
