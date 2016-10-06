-- Create table
create table CPT_NOTIFY
(
  platform     NUMBER not null,
  device_token VARCHAR2(100) not null,
  account_id   VARCHAR2(50),
  open_type    NUMBER not null,
  un_read_cnt  NUMBER not null,
  status       NUMBER not null,
  appid        VARCHAR2(36)
)
tablespace XCECS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table CPT_NOTIFY
  is 'ϵͳ��Ϣ֪ͨ��';
-- Add comments to the columns 
comment on column CPT_NOTIFY.platform
  is 'ƽ̨���[0:IOS,1:androdi]';
comment on column CPT_NOTIFY.device_token
  is 'IOS:APNS��deviceToken;android:appid+"#"+deviceId';
comment on column CPT_NOTIFY.account_id
  is '�û����';
comment on column CPT_NOTIFY.open_type
  is '�Ƿ��������[0:��1����]';
comment on column CPT_NOTIFY.un_read_cnt
  is '�û�δ����Ϣ��';
comment on column CPT_NOTIFY.appid
  is 'Ӧ��Ψһ��ʾ��';
-- Create/Recreate primary, unique and foreign key constraints 
alter table CPT_NOTIFY
  add constraint PK_CPT_NOTIFY primary key (DEVICE_TOKEN)
  using index 
  tablespace XCECS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
