package io.bos.accountmanager.di.component

import dagger.Component
import io.bos.accountmanager.MainActivity
import io.bos.accountmanager.di.PerActivity
import io.bos.accountmanager.di.module.ActivityModule
import io.bos.accountmanager.ui.*

@PerActivity
@Component(dependencies = [(AppComponent::class)], modules = [(ActivityModule::class)])
interface ActivityComponent {

    fun inject(activity: MainActivity)
    fun inject(walletManageActivity: WalletManageActivity)
    fun inject(quotaDeployActivity: QuotaDeployActivity)
    fun inject(secretSecurityActivity: SecretSecurityActivity)
    fun inject(pwdUpdateActivity: PwdUpdateActivity)
    fun inject(importAccountActivity: ImportAccountActivity)
    fun inject(authoritySettingActivity: AuthoritySettingActivity)
    fun inject(seniorSetUpActivity: SeniorSetUpActivity)
    fun inject(updatePowerActivity: UpdatePowerActivity)
    fun inject(newRedEnvelopesActivity: NewRedEnvelopesActivity)
    fun inject(oneDriveTestActivity: OneDriveTestActivity)
    fun inject(eosRedEnvelopeActivity: BosRedEnvelopeActivity)
    fun inject(cloudManagementActivity: CloudManagementActivity)
    fun inject(exportKeytTipsActivity: ExportKeytTipsActivity)
    fun inject(exportPrivateKeyActivity: ExportPrivateKeyActivity)
    fun inject(redEnvelopeRecordActivity: RedEnvelopeRecordActivity)
    fun inject(receiveRedEnvelopeActivity: ReceiveRedEnvelopeActivity)
    fun inject(createBosAccountActivity: CreateBosAccountActivity)
    fun inject(cloudImportListActivity: CloudImportListActivity)
    fun inject(redRecordActivity: RedRecordActivity)
    fun inject(accountListActivity: AccountListActivity)
    fun inject(redAddSuccessActivity: RedAddSuccessActivity)

    fun inject(redReceiveSuccessActivity: RedReceiveSuccessActivity)

    fun inject(externalImportActivity: ExternalImportActivity)
    fun inject(pwdConfirmUpdateActivity: PwdConfirmUpdateActivity)
}