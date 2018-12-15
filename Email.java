/**
 *@author: Henry Keena, Samuel Okikiola Ilesanmi, Bobby Sexton, Charlie Cohen
 *@version: 
**/


public class Email
{
	private String ip;
	private String sender;
	private String reciever;
	private String subject;
	private String message;
	private boolean isEncrypted;

	public Email(String _ip, String _sender, String _reciever, String _subject, String _message, boolean _isEncrypted)
	{
		ip = _ip;
		sender = _sender;			
		reciever = _reciever;
		subject = _subject;
		message = _message;
		isEncrypted  = _isEncrypted;
	}

	public String getIp()
	{
		return ip;
	}

	public String getSender()
	{
		return sender;
	}

	public String getReciever()
	{
		return reciever;
	}

	public String getSubject()
	{
		return subject;
	}

	public String getMessage()
	{
		return message;
	}

	public boolean getEnc()
	{
		return isEncrypted;
	}
}

