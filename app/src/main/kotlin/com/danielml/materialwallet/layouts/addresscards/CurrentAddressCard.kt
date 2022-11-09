package com.danielml.materialwallet.layouts.addresscards

import android.annotation.SuppressLint
import android.content.Context
import com.danielml.materialwallet.R
import org.bitcoinj.core.Address

@SuppressLint("ViewConstructor")
class CurrentAddressCard(context: Context, address: Address) : AddressCard(context, address, R.attr.materialCardViewFilledStyle)