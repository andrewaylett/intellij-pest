use std::alloc::{self, Layout};
use std::mem;

/// Allocate using the Rust allocator
///
/// # Safety
///
/// Must deallocate using the Rust allocator too
#[no_mangle]
pub unsafe extern "C" fn alloc(size: usize) -> *mut u8 {
    let layout = Layout::from_size_align(size, mem::align_of::<u8>()).unwrap();
    unsafe { alloc::alloc(layout) }
}

/// Deallocate using the Rust allocator
///
/// # Safety
///
/// Caller is responsible for avoiding use-after-free.
/// ptr must have been allocated using the rust allocator.
#[no_mangle]
pub unsafe extern "C" fn dealloc(ptr: *mut u8, size: usize) {
    let layout = Layout::from_size_align(size, mem::align_of::<u8>()).unwrap();
    unsafe {
        alloc::dealloc(ptr, layout);
    }
}
