/**
 *                            License
 * THE WORK (AS DEFINED BELOW) IS PROVIDED UNDER THE TERMS OF THIS  
 * CREATIVE COMMONS PUBLIC LICENSE ("CCPL" OR "LICENSE"). 
 * THE WORK IS PROTECTED BY COPYRIGHT AND/OR OTHER APPLICABLE LAW.  
 * ANY USE OF THE WORK OTHER THAN AS AUTHORIZED UNDER THIS LICENSE OR  
 * COPYRIGHT LAW IS PROHIBITED.
 * 
 * BY EXERCISING ANY RIGHTS TO THE WORK PROVIDED HERE, YOU ACCEPT AND  
 * AGREE TO BE BOUND BY THE TERMS OF THIS LICENSE. TO THE EXTENT THIS LICENSE  
 * MAY BE CONSIDERED TO BE A CONTRACT, THE LICENSOR GRANTS YOU THE RIGHTS CONTAINED 
 * HERE IN CONSIDERATION OF YOUR ACCEPTANCE OF SUCH TERMS AND CONDITIONS.
 * 
 */
package l1j.server.server.clientpackets;

import java.util.logging.Logger;

import l1j.server.server.ClientThread;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來建立組隊的封包
 */
public class C_CreateParty extends ClientBasePacket {

	private static final String C_CREATE_PARTY = "[C] C_CreateParty";
	private static Logger _log = Logger.getLogger(C_CreateParty.class
			.getName());

	public C_CreateParty(byte decrypt[], ClientThread client) throws Exception {
		super(decrypt);

		L1PcInstance pc = client.getActiveChar();

		int type = readC();
		if (type == 0 || type == 1) { // 自動接受組隊 on 與 off 的同
			int targetId = readD();
			L1Object temp = L1World.getInstance().findObject(targetId);
			if (temp instanceof L1PcInstance) {
				L1PcInstance targetPc = (L1PcInstance) temp;
				if (pc.getId() == targetPc.getId()) {
					return;
				}
				if (targetPc.isInParty()) {
					// 您無法邀請已經參加其他隊伍的人。
					pc.sendPackets(new S_ServerMessage(415));
					return;
				}

				if (pc.isInParty()) {
					if (pc.getParty().isLeader(pc)) {
						targetPc.setPartyID(pc.getId());
						// 玩家 %0%s 邀請您加入隊伍？(Y/N)
						targetPc.sendPackets(new S_Message_YN(953, pc
								.getName()));
					} else {
						// 只有領導者才能邀請其他的成員。
						pc.sendPackets(new S_ServerMessage(416));
					}
				} else {
					pc.setPartyType(type);
					targetPc.setPartyID(pc.getId());
					switch (type) {
					case 0:
						// 玩家 %0%s 邀請您加入隊伍？(Y/N)
						targetPc.sendPackets(new S_Message_YN(953, pc.getName()));
						break;
					case 1:
						targetPc.sendPackets(new S_Message_YN(954, pc.getName()));
						break;
					}
				}
			}
		} else if (type == 2) { // 聊天組隊
			String name = readS();
			L1PcInstance targetPc = L1World.getInstance().getPlayer(name);
			if (targetPc == null) {
				// 沒有叫%0的人。
				pc.sendPackets(new S_ServerMessage(109));
				return;
			}
			if (pc.getId() == targetPc.getId()) {
				return;
			}
			if (targetPc.isInChatParty()) {
				// 您無法邀請已經參加其他隊伍的人。
				pc.sendPackets(new S_ServerMessage(415));
				return;
			}

			if (pc.isInChatParty()) {
				if (pc.getChatParty().isLeader(pc)) {
					targetPc.setPartyID(pc.getId());
					// 您要接受玩家 %0%s 提出的隊伍對話邀請嗎？(Y/N)
					targetPc.sendPackets(new S_Message_YN(951, pc.getName()));
				} else {
					// 只有領導者才能邀請其他的成員。
					pc.sendPackets(new S_ServerMessage(416));
				}
			} else {
				targetPc.setPartyID(pc.getId());
				// 您要接受玩家 %0%s 提出的隊伍對話邀請嗎？(Y/N)
				targetPc.sendPackets(new S_Message_YN(951, pc.getName()));
			}
		}

	}

	@Override
	public String getType() {
		return C_CREATE_PARTY;
	}

}