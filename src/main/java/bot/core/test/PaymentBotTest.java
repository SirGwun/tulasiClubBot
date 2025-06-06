package bot.core.test;

public class PaymentBotTest {/*

    @InjectMocks
    private PaymentBot paymentBot;

    @Mock
    private Validator validator;

    @Mock
    private Message message;

    @Mock
    private Update update;

    @Mock
    private CallbackQuery callbackQuery;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testOnUpdateReceived_withMessage() {
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        paymentBot.onUpdateReceived(update);
        verify(paymentBot, times(1)).handleIncomingUpdate(message);
    }

    @Test
    public void testOnUpdateReceived_withCallbackQuery() {
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        paymentBot.onUpdateReceived(update);
        verify(paymentBot, times(1)).handleCallbackQuery(callbackQuery);
    }

    @Test
    public void testHandleIncomingUpdate_withCommand() {
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("/start");
        when(message.getChatId()).thenReturn(12345L);
        paymentBot.handleIncomingUpdate(message);
        verify(paymentBot, times(1)).handleCommand("/start", 12345L);
    }

    @Test
    public void testHandleCommand_start() {
        long userId = 12345L;
        paymentBot.handleCommand("/start", userId);
        verifyStatic(ChatUtils.class, times(1));
        ChatUtils.sendMessage(eq(userId), anyString());
    }

    @Test
    public void testHandleCommand_setGroup() {
        long userId = ConfigUtils.getAdminChatID();
        paymentBot.handleCommand("/set_group", userId);
        verifyStatic(ChatUtils.class, times(1));
        ChatUtils.getAllGroupKeyboard(eq(userId));
    }

    @Test
    public void testHandleCommand_newGroup() {
        long userId = ConfigUtils.getAdminChatID();
        paymentBot.handleCommand("/new_group", userId);
        verifyStatic(ChatUtils.class, times(1));
        ChatUtils.sendMessage(eq(userId), anyString());
        assertTrue(paymentBot.isCreatingNewGroup());
    }

    @Test
    public void testHandleCommand_cancel() {
        long userId = ConfigUtils.getAdminChatID();
        paymentBot.handleCommand("/cancel", userId);
        verifyStatic(ChatUtils.class, times(1));
        ChatUtils.sendMessage(eq(userId), anyString());
        assertFalse(paymentBot.isCreatingNewGroup());
    }

    @Test
    public void testHandleCommand_info() {
        long userId = 12345L;
        paymentBot.handleCommand("/info", userId);
        verifyStatic(ChatUtils.class, times(1));
        ChatUtils.sendMessage(eq(userId), anyString());
    }

    @Test
    public void testHandleCommand_help() {
        long userId = 12345L;
        paymentBot.handleCommand("/help", userId);
        verifyStatic(ChatUtils.class, times(1));
        ChatUtils.sendMessage(eq(userId), anyString());
    }

    @Test
    public void testHandleCommand_editInfo() {
        long userId = ConfigUtils.getAdminChatID();
        paymentBot.handleCommand("/edit_info", userId);
        verifyStatic(ChatUtils.class, times(1));
        ChatUtils.sendMessage(eq(userId), anyString());
        assertTrue(paymentBot.isEditingInfo());
    }

    @Test
    public void testHandleCommand_editHelp() {
        long userId = ConfigUtils.getAdminChatID();
        paymentBot.handleCommand("/edit_help", userId);
        verifyStatic(ChatUtils.class, times(1));
        ChatUtils.sendMessage(eq(userId), anyString());
        assertTrue(paymentBot.isEditingHelp());
    }

    @Test
    public void testHandleCommand_unknown() {
        long userId = 12345L;
        paymentBot.handleCommand("/unknown", userId);
        verifyStatic(ChatUtils.class, times(1));
        ChatUtils.sendMessage(eq(userId), eq("Неизвестная команда"));
    }

    @Test
    public void testHandleCallbackQuery_confirm() {
        when(callbackQuery.getData()).thenReturn("confirm_12345_67890");
        paymentBot.handleCallbackQuery(callbackQuery);
        verify(paymentBot, times(1)).handleConfirmAction(any(), anyInt(), anyLong(), anyLong(), anyInt());
    }

    @Test
    public void testHandleCallbackQuery_decline() {
        when(callbackQuery.getData()).thenReturn("decline_12345_67890");
        paymentBot.handleCallbackQuery(callbackQuery);
        verify(paymentBot, times(1)).handleDeclineAction(any(), anyInt(), anyLong(), anyLong(), anyInt());
    }

    @Test
    public void testHandleCallbackQuery_setGroup() {
        when(callbackQuery.getData()).thenReturn("setGroup_12345_67890");
        paymentBot.handleCallbackQuery(callbackQuery);
        verify(paymentBot, times(1)).handleSetGroupAction(any(), anyString(), anyLong(), anyInt());
    }

    @Test
    public void testHandleCallbackQuery_confirmAdmin() {
        when(callbackQuery.getData()).thenReturn("confirmAdmin_12345_67890");
        paymentBot.handleCallbackQuery(callbackQuery);
        verify(paymentBot, times(1)).handleConfirmAdminAction(any(), anyString(), anyLong());
    }

    @Test
    public void testProcessNewGroupCreation_validName() {
        when(message.getText()).thenReturn("validGroupName");
        paymentBot.processNewGroupCreation(message);
        assertEquals("validGroupName", paymentBot.getNewGroupName());
        assertFalse(paymentBot.isCreatingNewGroup());
        verifyStatic(ChatUtils.class, times(1));
        ChatUtils.sendMessage(anyLong(), anyString());
    }

    @Test
    public void testProcessNewGroupCreation_invalidName() {
        when(message.getText()).thenReturn("invalid group name");
        paymentBot.processNewGroupCreation(message);
        verifyStatic(ChatUtils.class, times(1));
        ChatUtils.sendMessage(anyLong(), eq("Некорректное имя группы"));
    }

    @Test
    public void testProcessNewGroupMember() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(paymentBot.getMe().getId());
        when(message.getNewChatMembers()).thenReturn(Collections.singletonList(user));
        paymentBot.processNewGroupMember(message);
        verifyStatic(ChatUtils.class, times(1));
        ChatUtils.getConfirmAdminStatusKeyboard(any(Group.class));
    }

    @Test
    public void testProcessInfoEditing() {
        when(message.getText()).thenReturn("new info");
        paymentBot.processInfoEditing(message);
        verifyStatic(ConfigUtils.class, times(1));
        ConfigUtils.setInfo(eq("new info"));
        assertFalse(paymentBot.isEditingInfo());
    }

    @Test
    public void testProcessHelpEditing() {
        when(message.getText()).thenReturn("new help");
        paymentBot.processHelpEditing(message);
        verifyStatic(ConfigUtils.class, times(1));
        ConfigUtils.setHelp(eq("new help"));
        assertFalse(paymentBot.isEditingHelp());
    }

    @Test
    public void testForwardMessageToAdmin() throws TelegramApiException {
        when(message.getChatId()).thenReturn(12345L);
        when(message.getMessageId()).thenReturn(1);
        paymentBot.forwardMessageToAdmin(message);
        verify(paymentBot, times(1)).execute(any(ForwardMessage.class));
    }

    @Test
    public void testHandleIncomingMessage_groupChat() {
        when(message.getChatId()).thenReturn(12345L);
        when(message.getChat().getType()).thenReturn("group");
        paymentBot.handleIncomingMessage(message);
        verifyStatic(Logger.class, times(1));
        LoggerFactory.getLogger(PaymentBot.class).info(anyString(), anyLong());
    }

    @Test
    public void testHandleIncomingMessage_userChat() {
        when(message.getChatId()).thenReturn(12345L);
        when(message.getChat().getType()).thenReturn("private");
        when(message.getFrom()).thenReturn(mock(User.class));
        paymentBot.handleIncomingMessage(message);
        verifyStatic(Logger.class, times(1));
        LoggerFactory.getLogger(PaymentBot.class).info(anyString(), anyLong());
    }

    @Test
    public void testHandlePayment_valid() {
        when(validator.isValidPayment(message)).thenReturn(true);
        paymentBot.handlePayment(message, 12345L, 67890L);
        verify(paymentBot, times(1)).addInGroup(67890L);
    }

    @Test
    public void testHandlePayment_invalid() {
        when(validator.isValidPayment(message)).thenReturn(false);
        paymentBot.handlePayment(message, 12345L, 67890L);
        verify(validator, times(1)).sendOuHumanValidation(message);
    }*/
}