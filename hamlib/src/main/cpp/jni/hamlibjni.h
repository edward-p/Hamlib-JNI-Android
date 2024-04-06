//
// Created by edward on 4/2/24.
//

#ifndef HAMLIB_JNI_H
#define HAMLIB_JNI_H

#include <hamlib/rig.h>

struct rig_data{
    int index;
    struct rig_caps *data;
};

#endif //LOOK4SAT_BINDINGS_H
