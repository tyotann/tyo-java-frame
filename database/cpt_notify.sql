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
  is '系统消息通知表';
-- Add comments to the columns 
comment on column CPT_NOTIFY.platform
  is '平台类别[0:IOS,1:androdi]';
comment on column CPT_NOTIFY.device_token
  is 'IOS:APNS的deviceToken;android:appid+"#"+deviceId';
comment on column CPT_NOTIFY.account_id
  is '用户编号';
comment on column CPT_NOTIFY.open_type
  is '是否接受推送[0:否，1：是]';
comment on column CPT_NOTIFY.un_read_cnt
  is '用户未读消息数';
comment on column CPT_NOTIFY.appid
  is '应用唯一标示符';
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
